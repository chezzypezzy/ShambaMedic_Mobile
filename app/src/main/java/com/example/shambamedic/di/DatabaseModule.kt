package com.example.shambamedic.di

import android.content.Context
import com.example.shambamedic.data.local.dao.*
import com.example.shambamedic.data.local.db.AppDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
        return AppDatabase.getInstance(context)
    }

    @Provides
    @Singleton
    fun provideUserDao(db: AppDatabase): UserDao = db.userDao()

    @Provides
    @Singleton
    fun provideDiseaseDao(db: AppDatabase): DiseaseDao = db.diseaseDao()

    @Provides
    @Singleton
    fun provideTreatmentDao(db: AppDatabase): TreatmentDao = db.treatmentDao()

    @Provides
    @Singleton
    fun provideScanDao(db: AppDatabase): ScanDao = db.scanDao()

    @Provides
    @Singleton
    fun provideSyncQueueDao(db: AppDatabase): SyncQueueDao = db.syncQueueDao()

    @Provides
    @Singleton
    fun provideCropGuideDao(db: AppDatabase): CropGuideDao = db.cropGuideDao()

    @Provides
    @Singleton
    fun provideEscalationDao(db: AppDatabase): EscalationDao = db.escalationDao()

    @Provides
    @Singleton
    fun provideRatingDao(db: AppDatabase): RatingDao = db.ratingDao()

    @Provides
    @Singleton
    fun provideNotificationDao(db: AppDatabase): NotificationDao = db.notificationDao()
}
