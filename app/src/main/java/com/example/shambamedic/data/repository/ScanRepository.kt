package com.example.shambamedic.data.repository

import com.example.shambamedic.data.local.dao.ScanDao
import com.example.shambamedic.data.local.dao.SyncQueueDao
import com.example.shambamedic.data.local.entity.SyncQueueEntity
import com.example.shambamedic.domain.model.Scan
import com.example.shambamedic.domain.model.toDomain
import com.example.shambamedic.domain.model.toEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.Calendar

data class DashboardStats(
    val totalDiseasesFound: Int,
    val healthyScansCount: Int,
    val pendingReviewCount: Int,
    val weeklyScanCounts: List<Int>,
    val mostCommonDisease: String?,
    val lastScanTimestamp: Long?
)

class ScanRepository(
    private val scanDao: ScanDao,
    private val syncQueueDao: SyncQueueDao
) {

    suspend fun saveScan(scan: Scan): Result<Scan> {
        return try {
            scanDao.insertScan(scan.toEntity())
            syncQueueDao.insertToQueue(SyncQueueEntity(scanId = scan.scanId))
            Result.success(scan)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun getScanHistory(userId: String): Flow<List<Scan>> {
        return scanDao.getScansByUser(userId).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    suspend fun getScanById(scanId: String): Scan? {
        return scanDao.getScanById(scanId)?.toDomain()
    }

    suspend fun updateSyncStatus(scanId: String, status: String) {
        scanDao.updateSyncStatus(scanId, status)
    }

    suspend fun getPendingScans(): List<Scan> {
        return scanDao.getPendingScans().map { it.toDomain() }
    }

    suspend fun getRecentScans(userId: String, limit: Int = 3): List<Scan> {
        return scanDao.getRecentScans(userId, limit).map { it.toDomain() }
    }

    suspend fun getDashboardStats(userId: String): DashboardStats {
        val diseaseCount = scanDao.getDiseaseCount(userId)
        val healthyCount = scanDao.getHealthyCount(userId)
        val pendingReviewCount = scanDao.getPendingReviewCount(userId)
        val lastScanTimestamp = scanDao.getLastScanTimestamp(userId)
        val mostCommonDisease = scanDao.getMostCommonDiseaseName(userId)

        val sevenDaysAgo = System.currentTimeMillis() - 7L * 24 * 60 * 60 * 1000
        val countsByDayOfWeek = scanDao.getWeeklyScanCounts(userId, sevenDaysAgo)
            .associate { it.day to it.count }

        // SQLite's strftime('%w', ...) returns 0 (Sunday) .. 6 (Saturday); Calendar.DAY_OF_WEEK
        // returns 1 (Sunday) .. 7 (Saturday), so subtracting 1 aligns the two.
        val weeklyScanCounts = (6 downTo 0).map { daysAgo ->
            val cal = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -daysAgo) }
            val dayOfWeek = (cal.get(Calendar.DAY_OF_WEEK) - 1).toString()
            countsByDayOfWeek[dayOfWeek] ?: 0
        }

        return DashboardStats(
            totalDiseasesFound = diseaseCount,
            healthyScansCount = healthyCount,
            pendingReviewCount = pendingReviewCount,
            weeklyScanCounts = weeklyScanCounts,
            mostCommonDisease = mostCommonDisease,
            lastScanTimestamp = lastScanTimestamp
        )
    }
}
