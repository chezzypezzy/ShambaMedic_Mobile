package com.example.shambamedic.domain.model

import com.example.shambamedic.data.local.entity.DiseaseEntity

data class Disease(
    val diseaseId: String,
    val diseaseName: String,
    val cropType: String,
    val symptomDescription: String,
    val severityScale: String
)

fun DiseaseEntity.toDomain() = Disease(
    diseaseId = diseaseId,
    diseaseName = diseaseName,
    cropType = cropType,
    symptomDescription = symptomDescription,
    severityScale = severityScale
)

fun Disease.toEntity() = DiseaseEntity(
    diseaseId = diseaseId,
    diseaseName = diseaseName,
    cropType = cropType,
    symptomDescription = symptomDescription,
    severityScale = severityScale
)
