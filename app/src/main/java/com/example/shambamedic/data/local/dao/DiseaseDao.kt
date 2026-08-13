package com.example.shambamedic.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.shambamedic.data.local.entity.DiseaseEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface DiseaseDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDisease(disease: DiseaseEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllDiseases(diseases: List<DiseaseEntity>)

    @Query("SELECT * FROM diseases WHERE disease_id = :diseaseId LIMIT 1")
    suspend fun getDiseaseById(diseaseId: String): DiseaseEntity?

    @Query("SELECT * FROM diseases WHERE disease_name = :diseaseName LIMIT 1")
    suspend fun getDiseaseByName(diseaseName: String): DiseaseEntity?

    @Query("SELECT * FROM diseases WHERE crop_type = :cropType")
    fun getDiseasesByCrop(cropType: String): Flow<List<DiseaseEntity>>

    @Query("SELECT * FROM diseases ORDER BY crop_type ASC")
    fun getAllDiseases(): Flow<List<DiseaseEntity>>
}
