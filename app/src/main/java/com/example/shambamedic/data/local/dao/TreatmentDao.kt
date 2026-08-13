package com.example.shambamedic.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.shambamedic.data.local.entity.TreatmentEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TreatmentDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTreatment(treatment: TreatmentEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllTreatments(treatments: List<TreatmentEntity>)

    @Query("SELECT * FROM treatments WHERE disease_id = :diseaseId")
    suspend fun getTreatmentsForDisease(diseaseId: String): List<TreatmentEntity>

    @Query("SELECT * FROM treatments WHERE disease_id = :diseaseId")
    fun getTreatmentsForDiseaseFlow(diseaseId: String): Flow<List<TreatmentEntity>>

    @Query("DELETE FROM treatments")
    suspend fun deleteAllTreatments()
}
