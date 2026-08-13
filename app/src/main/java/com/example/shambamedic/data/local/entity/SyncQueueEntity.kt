package com.example.shambamedic.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(
    tableName = "sync_queue",
    foreignKeys = [
        ForeignKey(
            entity = ScanEntity::class,
            parentColumns = ["scan_id"],
            childColumns = ["scan_id"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class SyncQueueEntity(
    @PrimaryKey
    @ColumnInfo(name = "queue_id")
    val queueId: String = UUID.randomUUID().toString(),

    @ColumnInfo(name = "scan_id")
    val scanId: String,

    @ColumnInfo(name = "attempt_count")
    val attemptCount: Int = 0,

    @ColumnInfo(name = "last_attempt_timestamp")
    val lastAttemptTimestamp: Long? = null,

    @ColumnInfo(name = "error_message")
    val errorMessage: String? = null
)
