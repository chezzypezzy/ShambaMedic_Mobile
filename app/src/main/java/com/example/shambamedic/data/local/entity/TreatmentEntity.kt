package com.example.shambamedic.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "treatments",
    foreignKeys = [
        ForeignKey(
            entity = DiseaseEntity::class,
            parentColumns = ["disease_id"],
            childColumns = ["disease_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["disease_id"])]
)
data class TreatmentEntity(
    @PrimaryKey
    @ColumnInfo(name = "treatment_id")
    val treatmentId: String,

    @ColumnInfo(name = "disease_id")
    val diseaseId: String,

    @ColumnInfo(name = "intervention_type")
    val interventionType: String,

    @ColumnInfo(name = "product_names")
    val productNames: String,

    @ColumnInfo(name = "application_method")
    val applicationMethod: String,

    @ColumnInfo(name = "dosage")
    val dosage: String,

    @ColumnInfo(name = "cost_range_kes")
    val costRangeKes: String
)
