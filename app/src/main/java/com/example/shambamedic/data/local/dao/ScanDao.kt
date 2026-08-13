package com.example.shambamedic.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.shambamedic.data.local.entity.ScanEntity
import kotlinx.coroutines.flow.Flow

data class DayCount(
    val day: String,
    val count: Int
)

@Dao
interface ScanDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertScan(scan: ScanEntity)

    @Query("SELECT * FROM scans WHERE user_id = :userId ORDER BY scan_timestamp DESC")
    fun getScansByUser(userId: String): Flow<List<ScanEntity>>

    @Query("SELECT * FROM scans WHERE scan_id = :scanId LIMIT 1")
    suspend fun getScanById(scanId: String): ScanEntity?

    @Query("SELECT * FROM scans WHERE sync_status = 'pending'")
    suspend fun getPendingScans(): List<ScanEntity>

    @Query("UPDATE scans SET sync_status = :status WHERE scan_id = :scanId")
    suspend fun updateSyncStatus(scanId: String, status: String)

    @Query("DELETE FROM scans WHERE scan_id = :scanId")
    suspend fun deleteScan(scanId: String)

    @Query("SELECT COUNT(*) FROM scans WHERE user_id = :userId")
    fun getScanCountByUser(userId: String): Flow<Int>

    @Query("SELECT * FROM scans WHERE user_id = :userId ORDER BY scan_timestamp DESC LIMIT :limit")
    suspend fun getRecentScans(userId: String, limit: Int): List<ScanEntity>

    @Query("SELECT COUNT(*) FROM scans WHERE user_id = :userId AND disease_id IS NOT NULL")
    suspend fun getDiseaseCount(userId: String): Int

    @Query(
        """
        SELECT COUNT(*) FROM scans
        WHERE user_id = :userId AND disease_id IS NULL
        AND NOT EXISTS (
            SELECT 1 FROM escalations
            WHERE escalations.scan_id = scans.scan_id AND escalations.status = 'pending'
        )
        """
    )
    suspend fun getHealthyCount(userId: String): Int

    @Query(
        """
        SELECT COUNT(*) FROM scans
        INNER JOIN escalations ON escalations.scan_id = scans.scan_id
        WHERE scans.user_id = :userId AND escalations.status = 'pending'
        """
    )
    suspend fun getPendingReviewCount(userId: String): Int

    @Query("SELECT scan_timestamp FROM scans WHERE user_id = :userId ORDER BY scan_timestamp DESC LIMIT 1")
    suspend fun getLastScanTimestamp(userId: String): Long?

    @Query(
        """
        SELECT strftime('%w', scan_timestamp / 1000, 'unixepoch') as day,
        COUNT(*) as count
        FROM scans WHERE user_id = :userId
        AND scan_timestamp >= :sevenDaysAgo
        GROUP BY day
        """
    )
    suspend fun getWeeklyScanCounts(userId: String, sevenDaysAgo: Long): List<DayCount>

    @Query(
        """
        SELECT d.disease_name FROM scans s
        JOIN diseases d ON s.disease_id = d.disease_id
        WHERE s.user_id = :userId
        GROUP BY s.disease_id
        ORDER BY COUNT(*) DESC LIMIT 1
        """
    )
    suspend fun getMostCommonDiseaseName(userId: String): String?
}
