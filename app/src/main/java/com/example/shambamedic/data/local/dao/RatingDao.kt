package com.example.shambamedic.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.shambamedic.data.local.entity.RatingEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface RatingDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRating(rating: RatingEntity)

    @Query("SELECT * FROM ratings WHERE escalation_id = :escalationId")
    suspend fun getRatingsForEscalation(escalationId: String): List<RatingEntity>

    @Query("SELECT AVG(star_rating) FROM ratings WHERE rated_role = :role")
    fun getAverageRatingForRole(role: String): Flow<Float?>

    @Query("SELECT * FROM ratings WHERE sync_status = 'pending'")
    suspend fun getPendingSyncRatings(): List<RatingEntity>

    @Query("UPDATE ratings SET sync_status = :status WHERE rating_id = :ratingId")
    suspend fun updateSyncStatus(ratingId: String, status: String)
}
