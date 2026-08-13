package com.example.shambamedic.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.shambamedic.data.local.entity.SyncQueueEntity

@Dao
interface SyncQueueDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertToQueue(entry: SyncQueueEntity)

    @Query("SELECT * FROM sync_queue ORDER BY queue_id ASC")
    suspend fun getAllPendingEntries(): List<SyncQueueEntity>

    @Query("SELECT * FROM sync_queue WHERE scan_id = :scanId LIMIT 1")
    suspend fun getEntryByScanId(scanId: String): SyncQueueEntity?

    @Query("UPDATE sync_queue SET attempt_count = :attemptCount, last_attempt_timestamp = :timestamp, error_message = :errorMessage WHERE queue_id = :queueId")
    suspend fun updateAttempt(queueId: String, attemptCount: Int, timestamp: Long, errorMessage: String?)

    @Query("DELETE FROM sync_queue WHERE queue_id = :queueId")
    suspend fun deleteEntry(queueId: String)

    @Query("DELETE FROM sync_queue")
    suspend fun clearQueue()
}
