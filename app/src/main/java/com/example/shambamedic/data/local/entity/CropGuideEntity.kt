package com.example.shambamedic.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "crop_guides")
data class CropGuideEntity(
    @PrimaryKey
    @ColumnInfo(name = "guide_id")
    val guideId: String,

    @ColumnInfo(name = "crop_type")
    val cropType: String, // "maize", "potato", "tomato"

    @ColumnInfo(name = "planting_calendar")
    val plantingCalendar: String,

    @ColumnInfo(name = "agronomic_guidance")
    val agronomicGuidance: String
)
