package com.example.shambamedic.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.shambamedic.data.local.entity.EscalationEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface EscalationDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEscalation(escalation: EscalationEntity)

    @Query("SELECT * FROM escalations WHERE scan_id = :scanId LIMIT 1")
    suspend fun getEscalationByScanId(scanId: String): EscalationEntity?

    @Query("SELECT * FROM escalations WHERE status = 'pending'")
    suspend fun getPendingEscalations(): List<EscalationEntity>

    @Query("UPDATE escalations SET botanist_diagnosis = :diagnosis, status = 'resolved', resolved_timestamp = :resolvedTimestamp WHERE escalation_id = :escalationId")
    suspend fun updateDiagnosis(escalationId: String, diagnosis: String, resolvedTimestamp: Long)

    /**
     * Locally-pending escalations whose parent scan has already synchronized. The backend
     * 404s an escalation sync whose scan_id it doesn't recognize yet, so scan sync must
     * complete first.
     */
    @Query(
        "SELECT escalations.* FROM escalations " +
            "INNER JOIN scans ON escalations.scan_id = scans.scan_id " +
            "WHERE escalations.sync_status = 'pending' AND scans.sync_status = 'synchronized'"
    )
    suspend fun getPendingSyncEscalationsWithSyncedScan(): List<EscalationEntity>

    @Query("UPDATE escalations SET sync_status = :status WHERE escalation_id = :escalationId")
    suspend fun updateSyncStatus(escalationId: String, status: String)

    @Query(
        "SELECT escalations.* FROM escalations " +
            "INNER JOIN scans ON escalations.scan_id = scans.scan_id " +
            "WHERE scans.user_id = :userId"
    )
    fun getAllEscalationsForUser(userId: String): Flow<List<EscalationEntity>>
}
