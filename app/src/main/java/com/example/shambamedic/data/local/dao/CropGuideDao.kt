package com.example.shambamedic.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.shambamedic.data.local.entity.CropGuideEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CropGuideDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGuide(guide: CropGuideEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllGuides(guides: List<CropGuideEntity>)

    @Query("SELECT * FROM crop_guides WHERE crop_type = :cropType LIMIT 1")
    suspend fun getGuideByCrop(cropType: String): CropGuideEntity?

    @Query("SELECT * FROM crop_guides ORDER BY crop_type ASC")
    fun getAllGuides(): Flow<List<CropGuideEntity>>
}
