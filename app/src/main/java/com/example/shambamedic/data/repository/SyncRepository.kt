package com.example.shambamedic.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import com.example.shambamedic.BuildConfig
import com.example.shambamedic.data.local.dao.EscalationDao
import com.example.shambamedic.data.local.dao.RatingDao
import com.example.shambamedic.data.local.dao.ScanDao
import com.example.shambamedic.data.local.dao.SyncQueueDao
import com.example.shambamedic.data.local.dao.TreatmentDao
import com.example.shambamedic.data.local.dao.UserDao
import com.example.shambamedic.data.remote.api.ShambaMedicApi
import com.example.shambamedic.data.remote.dto.EscalationSyncDto
import com.example.shambamedic.data.remote.dto.RatingSyncDto
import com.example.shambamedic.data.remote.dto.ScanSyncDto
import com.example.shambamedic.data.remote.dto.toDto
import com.example.shambamedic.domain.model.toDomain
import com.example.shambamedic.domain.model.toEntity
import com.example.shambamedic.util.Constants
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import retrofit2.HttpException
import java.io.IOException

/**
 * Note: pushPendingScans()/pushPendingEscalations()/pushPendingRatings()/
 * pollResolvedEscalations() below target the real, verified backend contract (the
 * api/sync endpoints, header X-API-Key). syncPendingScans()/checkForTreatmentUpdates()
 * further down target an older api/scans contract from earlier offline-first sync
 * scaffolding, predate this contract, and are no longer invoked by SyncManager - see
 * SyncManager for the wiring. Kept in place rather than deleted to avoid an unplanned
 * schema/entity removal (SyncQueueEntity); safe to remove in a follow-up cleanup pass.
 */
class SyncRepository(
    private val syncQueueDao: SyncQueueDao,
    private val scanDao: ScanDao,
    private val treatmentDao: TreatmentDao,
    private val escalationDao: EscalationDao,
    private val ratingDao: RatingDao,
    private val userDao: UserDao,
    private val notificationRepository: NotificationRepository,
    private val api: ShambaMedicApi,
    private val dataStore: DataStore<Preferences>
) {

    private val apiKey: String get() = BuildConfig.SYNC_API_KEY

    /**
     * Runs the full real sync sequence in the required order: scans, then escalations
     * (which depend on their parent scan already being synchronized), then ratings, then
     * polling for botanist resolutions. Each step catches its own per-item failures, so a
     * failure in one step never blocks the next - offline-first sync failures are routine,
     * not exceptional.
     */
    suspend fun syncAll(): Result<Int> {
        var total = 0
        pushPendingScans().onSuccess { total += it }
        pushPendingEscalations().onSuccess { total += it }
        pushPendingRatings().onSuccess { total += it }
        pollResolvedEscalations().onSuccess { total += it }
        return Result.success(total)
    }

    suspend fun pushPendingScans(): Result<Int> {
        return try {
            var count = 0
            for (scan in scanDao.getPendingScans()) {
                val farmer = userDao.getUserById(scan.userId)
                val dto = ScanSyncDto(
                    scanId = scan.scanId,
                    farmerRef = scan.userId,
                    farmerName = farmer?.name,
                    // A Google-only account's phoneNumber holds a synthetic placeholder
                    // ("google_<id>"), not a real phone number - don't send that as
                    // farmer_phone, which is nullable exactly for cases like this.
                    farmerPhone = farmer?.let { if (it.authProvider == "google") null else it.phoneNumber },
                    diseaseId = scan.diseaseId,
                    cropType = scan.cropType,
                    confidenceScore = scan.confidenceScore,
                    severity = scan.severity,
                    scanTimestamp = scan.scanTimestamp,
                    latitude = scan.latitude,
                    longitude = scan.longitude
                )
                try {
                    api.syncScan(apiKey, dto)
                    scanDao.updateSyncStatus(scan.scanId, "synchronized")
                    count++
                } catch (e: HttpException) {
                    // Genuine validation/auth errors won't succeed on retry without a code
                    // fix. 404/408/429 and other 5xx can indicate misconfiguration or a
                    // transient backend issue (e.g. a wrong base URL) rather than bad data,
                    // so leave those pending for retry - matches pushPendingEscalations()/
                    // pushPendingRatings() below.
                    if (e.code() in setOf(400, 401, 403, 422)) {
                        scanDao.updateSyncStatus(scan.scanId, "failed")
                    }
                } catch (e: IOException) {
                    // No connectivity/timeout: leave pending for retry next cycle.
                }
            }
            Result.success(count)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun pushPendingEscalations(): Result<Int> {
        return try {
            var count = 0
            for (escalation in escalationDao.getPendingSyncEscalationsWithSyncedScan()) {
                val dto = EscalationSyncDto(
                    escalationId = escalation.escalationId,
                    scanId = escalation.scanId,
                    escalatedTimestamp = escalation.escalatedTimestamp
                )
                try {
                    api.syncEscalation(apiKey, dto)
                    escalationDao.updateSyncStatus(escalation.escalationId, "synchronized")
                    count++
                } catch (e: HttpException) {
                    // 404 means the backend doesn't recognize scan_id yet, despite our local
                    // ordering gate - leave pending rather than failed, it may resolve once
                    // server-side state catches up. 408/429 can indicate a transient or
                    // rate-limited backend rather than bad data, so also leave those pending -
                    // matches pushPendingScans() above. Other 4xx won't succeed on retry.
                    if (e.code() in 400..499 && e.code() !in setOf(404, 408, 429)) {
                        escalationDao.updateSyncStatus(escalation.escalationId, "failed")
                    }
                } catch (e: IOException) {
                    // No connectivity/timeout: leave pending for retry next cycle.
                }
            }
            Result.success(count)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun pushPendingRatings(): Result<Int> {
        return try {
            var count = 0
            for (rating in ratingDao.getPendingSyncRatings()) {
                val dto = RatingSyncDto(
                    ratingId = rating.ratingId,
                    escalationId = rating.escalationId,
                    starRating = rating.starRating,
                    comment = rating.comment,
                    ratingTimestamp = rating.ratingTimestamp,
                    ratedBy = rating.ratedBy
                )
                try {
                    api.syncRating(apiKey, dto)
                    ratingDao.updateSyncStatus(rating.ratingId, "synchronized")
                    count++
                } catch (e: HttpException) {
                    when (e.code()) {
                        // Escalation isn't resolved yet server-side: expected transient
                        // state, not an error - leave pending for retry next cycle.
                        409 -> Unit
                        // escalation_id not yet known server-side: leave pending, it may
                        // resolve once the escalation itself finishes syncing.
                        404 -> Unit
                        // Transient/rate-limited backend rather than bad data - leave
                        // pending, matches pushPendingScans()/pushPendingEscalations().
                        408, 429 -> Unit
                        else -> ratingDao.updateSyncStatus(rating.ratingId, "failed")
                    }
                } catch (e: IOException) {
                    // No connectivity/timeout: leave pending for retry next cycle.
                }
            }
            Result.success(count)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun pollResolvedEscalations(): Result<Int> {
        return try {
            val userId = dataStore.data.map { it[Constants.USER_ID_KEY] }.first() ?: return Result.success(0)
            val resolved = api.getResolvedEscalations(apiKey, farmerRef = userId, status = "resolved")
            var updatedCount = 0
            for (remote in resolved) {
                val local = escalationDao.getEscalationByScanId(remote.scanId) ?: continue
                if (local.status == "pending" && remote.botanistDiagnosis != null && remote.resolvedTimestamp != null) {
                    escalationDao.updateDiagnosis(local.escalationId, remote.botanistDiagnosis, remote.resolvedTimestamp)
                    val scan = scanDao.getScanById(remote.scanId)
                    if (scan != null) {
                        notificationRepository.createNotification(
                            userId = scan.userId,
                            title = "Diagnosis Received", // TODO: Add Swahili
                            message = "A botanist has reviewed your scan and provided " +
                                "a diagnosis. Tap to view the result.", // TODO: Add Swahili
                            relatedScanId = remote.scanId
                        )
                    }
                    updatedCount++
                }
            }
            Result.success(updatedCount)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun syncPendingScans(): Result<Int> {
        return try {
            val pendingEntries = syncQueueDao.getAllPendingEntries()
            if (pendingEntries.isEmpty()) return Result.success(0)

            var successCount = 0
            for (entry in pendingEntries) {
                val scanEntity = scanDao.getScanById(entry.scanId)
                if (scanEntity == null) {
                    syncQueueDao.deleteEntry(entry.queueId)
                    continue
                }

                try {
                    api.uploadScan(scanEntity.toDto())
                    scanDao.updateSyncStatus(entry.scanId, "synchronized")
                    syncQueueDao.deleteEntry(entry.queueId)
                    successCount++
                } catch (itemError: Exception) {
                    val nextAttempt = entry.attemptCount + 1
                    if (nextAttempt >= Constants.SYNC_MAX_ATTEMPTS) {
                        scanDao.updateSyncStatus(entry.scanId, "failed")
                        syncQueueDao.deleteEntry(entry.queueId)
                    } else {
                        syncQueueDao.updateAttempt(
                            queueId = entry.queueId,
                            attemptCount = nextAttempt,
                            timestamp = System.currentTimeMillis(),
                            errorMessage = itemError.message ?: "Unknown error"
                        )
                    }
                }
            }
            Result.success(successCount)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun checkForTreatmentUpdates(): Result<Boolean> {
        return try {
            val updates = api.getTreatmentUpdates()
            if (updates.isNotEmpty()) {
                // For simplicity in this demo, we just clear and re-insert
                // In production, we would use a more sophisticated diff/sync strategy
                treatmentDao.deleteAllTreatments()
                // Mapping DTO to Entity would happen here if we had more info
                // For now, return success to indicate "updates were processed"
                Result.success(true)
            } else {
                Result.success(false)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
