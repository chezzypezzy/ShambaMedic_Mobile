package com.example.shambamedic.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(
    tableName = "ratings",
    foreignKeys = [
        ForeignKey(
            entity = EscalationEntity::class,
            parentColumns = ["escalation_id"],
            childColumns = ["escalation_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["escalation_id"])
    ]
)
data class RatingEntity(
    @PrimaryKey
    @ColumnInfo(name = "rating_id")
    val ratingId: String = UUID.randomUUID().toString(),

    @ColumnInfo(name = "escalation_id")
    val escalationId: String,

    @ColumnInfo(name = "rated_by")
    val ratedBy: String,

    /**
     * Conceptually constrained to "farmer" or "botanist"; Room does not enforce enums on String columns.
     */
    @ColumnInfo(name = "rated_role")
    val ratedRole: String,

    @ColumnInfo(name = "star_rating")
    val starRating: Int,

    @ColumnInfo(name = "comment")
    val comment: String? = null,

    @ColumnInfo(name = "rating_timestamp")
    val ratingTimestamp: Long,

    /**
     * "pending" | "synchronized" | "failed". Mirrors ScanEntity.syncStatus's convention.
     */
    @ColumnInfo(name = "sync_status")
    val syncStatus: String = "pending"
)
