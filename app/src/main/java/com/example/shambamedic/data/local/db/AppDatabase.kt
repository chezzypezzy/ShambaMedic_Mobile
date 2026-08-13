package com.example.shambamedic.data.local.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.shambamedic.data.local.dao.*
import com.example.shambamedic.data.local.entity.*

@Database(
    entities = [
        UserEntity::class,
        DiseaseEntity::class,
        TreatmentEntity::class,
        ScanEntity::class,
        SyncQueueEntity::class,
        CropGuideEntity::class,
        EscalationEntity::class,
        RatingEntity::class,
        NotificationEntity::class
    ],
    version = 6,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun userDao(): UserDao
    abstract fun diseaseDao(): DiseaseDao
    abstract fun treatmentDao(): TreatmentDao
    abstract fun scanDao(): ScanDao
    abstract fun syncQueueDao(): SyncQueueDao
    abstract fun cropGuideDao(): CropGuideDao
    abstract fun escalationDao(): EscalationDao
    abstract fun ratingDao(): RatingDao
    abstract fun notificationDao(): NotificationDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "shambamedic_db"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
