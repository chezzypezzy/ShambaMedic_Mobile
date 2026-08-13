package com.example.shambamedic.domain.model

import com.example.shambamedic.data.local.entity.TreatmentEntity

data class Treatment(
    val treatmentId: String,
    val diseaseId: String,
    val interventionType: String,
    val productNames: String,
    val applicationMethod: String,
    val dosage: String,
    val costRangeKes: String
)

fun TreatmentEntity.toDomain() = Treatment(
    treatmentId = treatmentId,
    diseaseId = diseaseId,
    interventionType = interventionType,
    productNames = productNames,
    applicationMethod = applicationMethod,
    dosage = dosage,
    costRangeKes = costRangeKes
)

fun Treatment.toEntity() = TreatmentEntity(
    treatmentId = treatmentId,
    diseaseId = diseaseId,
    interventionType = interventionType,
    productNames = productNames,
    applicationMethod = applicationMethod,
    dosage = dosage,
    costRangeKes = costRangeKes
)
