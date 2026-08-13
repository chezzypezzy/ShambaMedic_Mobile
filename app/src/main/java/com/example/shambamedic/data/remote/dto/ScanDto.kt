package com.example.shambamedic.data.remote.dto

import com.example.shambamedic.data.local.entity.ScanEntity
import com.example.shambamedic.domain.model.Scan

data class ScanDto(
    val scanId: String,
    val userId: String,
    val diseaseId: String?,
    val confidenceScore: Float,
    val severity: String,
    val latitude: Double?,
    val longitude: Double?,
    val scanTimestamp: Long
)

fun Scan.toDto() = ScanDto(
    scanId = scanId,
    userId = userId,
    diseaseId = diseaseId,
    confidenceScore = confidenceScore,
    severity = severity,
    latitude = latitude,
    longitude = longitude,
    scanTimestamp = scanTimestamp
)

fun ScanEntity.toDto() = ScanDto(
    scanId = scanId,
    userId = userId,
    diseaseId = diseaseId,
    confidenceScore = confidenceScore,
    severity = severity,
    latitude = latitude,
    longitude = longitude,
    scanTimestamp = scanTimestamp
)
