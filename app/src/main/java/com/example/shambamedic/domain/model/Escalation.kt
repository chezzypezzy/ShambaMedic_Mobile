package com.example.shambamedic.domain.model

import com.example.shambamedic.data.local.entity.EscalationEntity

data class Escalation(
    val escalationId: String,
    val scanId: String,
    val botanistDiagnosis: String?,
    val status: String,
    val escalatedTimestamp: Long,
    val resolvedTimestamp: Long?,
    val syncStatus: String
)

fun EscalationEntity.toDomain() = Escalation(
    escalationId = escalationId,
    scanId = scanId,
    botanistDiagnosis = botanistDiagnosis,
    status = status,
    escalatedTimestamp = escalatedTimestamp,
    resolvedTimestamp = resolvedTimestamp,
    syncStatus = syncStatus
)

fun Escalation.toEntity() = EscalationEntity(
    escalationId = escalationId,
    scanId = scanId,
    botanistDiagnosis = botanistDiagnosis,
    status = status,
    escalatedTimestamp = escalatedTimestamp,
    resolvedTimestamp = resolvedTimestamp,
    syncStatus = syncStatus
)
