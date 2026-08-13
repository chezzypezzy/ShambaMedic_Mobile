package com.example.shambamedic.domain.model

import com.example.shambamedic.data.local.entity.CropGuideEntity

data class CropGuide(
    val guideId: String,
    val cropType: String,
    val plantingCalendar: String,
    val agronomicGuidance: String
)

fun CropGuideEntity.toDomain() = CropGuide(
    guideId = guideId,
    cropType = cropType,
    plantingCalendar = plantingCalendar,
    agronomicGuidance = agronomicGuidance
)

fun CropGuide.toEntity() = CropGuideEntity(
    guideId = guideId,
    cropType = cropType,
    plantingCalendar = plantingCalendar,
    agronomicGuidance = agronomicGuidance
)
