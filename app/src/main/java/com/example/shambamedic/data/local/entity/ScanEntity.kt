package com.example.shambamedic.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(
    tableName = "scans",
    foreignKeys = [
        ForeignKey(
            entity = UserEntity::class,
            parentColumns = ["user_id"],
            childColumns = ["user_id"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = DiseaseEntity::class,
            parentColumns = ["disease_id"],
            childColumns = ["disease_id"],
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [
        Index(value = ["user_id"]),
        Index(value = ["disease_id"])
    ]
)
data class ScanEntity(
    @PrimaryKey
    @ColumnInfo(name = "scan_id")
    val scanId: String = UUID.randomUUID().toString(),

    @ColumnInfo(name = "user_id")
    val userId: String,

    @ColumnInfo(name = "disease_id")
    val diseaseId: String?,

    @ColumnInfo(name = "crop_type")
    val cropType: String,

    /**
     * Room handles ByteArray natively, so no custom TypeConverter is required.
     */
    @ColumnInfo(name = "image_thumbnail", typeAffinity = ColumnInfo.BLOB)
    val imageThumbnail: ByteArray? = null,

    @ColumnInfo(name = "confidence_score")
    val confidenceScore: Float,

    @ColumnInfo(name = "severity")
    val severity: String,

    @ColumnInfo(name = "latitude")
    val latitude: Double? = null,

    @ColumnInfo(name = "longitude")
    val longitude: Double? = null,

    @ColumnInfo(name = "scan_timestamp")
    val scanTimestamp: Long,

    @ColumnInfo(name = "sync_status")
    val syncStatus: String = "pending"
)
