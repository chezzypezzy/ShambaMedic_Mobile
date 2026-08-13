package com.example.shambamedic.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(
    tableName = "escalations",
    foreignKeys = [
        ForeignKey(
            entity = ScanEntity::class,
            parentColumns = ["scan_id"],
            childColumns = ["scan_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["scan_id"])
    ]
)
data class EscalationEntity(
    @PrimaryKey
    @ColumnInfo(name = "escalation_id")
    val escalationId: String = UUID.randomUUID().toString(),

    @ColumnInfo(name = "scan_id")
    val scanId: String,

    @ColumnInfo(name = "botanist_diagnosis")
    val botanistDiagnosis: String? = null,

    @ColumnInfo(name = "status")
    val status: String = "pending",

    @ColumnInfo(name = "escalated_timestamp")
    val escalatedTimestamp: Long,

    @ColumnInfo(name = "resolved_timestamp")
    val resolvedTimestamp: Long? = null,

    /**
     * Sync-tracking state, separate from [status] (the pending/resolved workflow state):
     * "pending" | "synchronized" | "failed". Mirrors ScanEntity.syncStatus's convention.
     */
    @ColumnInfo(name = "sync_status")
    val syncStatus: String = "pending"
)
