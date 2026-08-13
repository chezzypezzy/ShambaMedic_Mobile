package com.example.shambamedic.data.repository

import com.example.shambamedic.data.local.dao.EscalationDao
import com.example.shambamedic.data.local.dao.ScanDao
import com.example.shambamedic.data.local.entity.EscalationEntity
import com.example.shambamedic.domain.model.Escalation
import com.example.shambamedic.domain.model.toDomain
import java.util.UUID

class EscalationRepository(
    private val escalationDao: EscalationDao,
    private val scanDao: ScanDao,
    private val notificationRepository: NotificationRepository
) {

    suspend fun createEscalation(scanId: String): Result<Escalation> {
        return try {
            val escalation = EscalationEntity(
                escalationId = UUID.randomUUID().toString(),
                scanId = scanId,
                status = "pending",
                escalatedTimestamp = System.currentTimeMillis()
            )
            escalationDao.insertEscalation(escalation)
            Result.success(escalation.toDomain())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getEscalationForScan(scanId: String): Escalation? {
        return escalationDao.getEscalationByScanId(scanId)?.toDomain()
    }

    suspend fun getPendingEscalations(): List<Escalation> {
        return escalationDao.getPendingEscalations().map { it.toDomain() }
    }

    // DEBUG ONLY, offline UI testing only: simulates a botanist response locally without
    // a deployed backend. Using this while also testing real sync (SyncRepository.
    // pollResolvedEscalations) will create local data that can never match the backend -
    // don't mix the two in the same test session.
    suspend fun simulateResolution(
        scanId: String,
        diagnosis: String
    ): Result<Unit> {
        val escalation = escalationDao.getEscalationByScanId(scanId)
            ?: return Result.failure(Exception("No escalation found"))
        escalationDao.updateDiagnosis(
            escalation.escalationId,
            diagnosis,
            System.currentTimeMillis()
        )

        val scan = scanDao.getScanById(scanId)
        if (scan != null) {
            notificationRepository.createNotification(
                userId = scan.userId,
                title = "Diagnosis Received", // TODO: Add Swahili
                message = "A botanist has reviewed your scan and provided " +
                    "a diagnosis. Tap to view the result.", // TODO: Add Swahili
                relatedScanId = scanId
            )
        }
        return Result.success(Unit)
    }
}
