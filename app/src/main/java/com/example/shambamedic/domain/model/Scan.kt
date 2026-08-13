package com.example.shambamedic.domain.model

import com.example.shambamedic.data.local.entity.ScanEntity

data class Scan(
    val scanId: String,
    val userId: String,
    val diseaseId: String?,
    val cropType: String,
    val imageThumbnail: ByteArray?,
    val confidenceScore: Float,
    /**
     * "low" | "moderate" | "severe", set once by ClassificationResult.calculateSeverity()
     * at scan-creation time and never overwritten. Escalation/review workflow state lives
     * separately on Escalation.status - see EscalationDao.getEscalationByScanId().
     */
    val severity: String,
    val latitude: Double?,
    val longitude: Double?,
    val scanTimestamp: Long,
    val syncStatus: String
)

fun ScanEntity.toDomain() = Scan(
    scanId = scanId,
    userId = userId,
    diseaseId = diseaseId,
    cropType = cropType,
    imageThumbnail = imageThumbnail,
    confidenceScore = confidenceScore,
    severity = severity,
    latitude = latitude,
    longitude = longitude,
    scanTimestamp = scanTimestamp,
    syncStatus = syncStatus
)

fun Scan.toEntity() = ScanEntity(
    scanId = scanId,
    userId = userId,
    diseaseId = diseaseId,
    cropType = cropType,
    imageThumbnail = imageThumbnail,
    confidenceScore = confidenceScore,
    severity = severity,
    latitude = latitude,
    longitude = longitude,
    scanTimestamp = scanTimestamp,
    syncStatus = syncStatus
)
