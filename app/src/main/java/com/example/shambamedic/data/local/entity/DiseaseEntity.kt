package com.example.shambamedic.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "diseases")
data class DiseaseEntity(
    @PrimaryKey
    @ColumnInfo(name = "disease_id")
    val diseaseId: String,

    @ColumnInfo(name = "disease_name")
    val diseaseName: String,

    @ColumnInfo(name = "crop_type")
    val cropType: String, // "maize", "potato", "tomato"

    @ColumnInfo(name = "symptom_description")
    val symptomDescription: String,

    @ColumnInfo(name = "severity_scale")
    val severityScale: String
)
