package com.example.shambamedic.di

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import com.example.shambamedic.data.local.dao.*
import com.example.shambamedic.data.remote.api.ShambaMedicApi
import com.example.shambamedic.data.repository.*
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {

    @Provides
    @Singleton
    fun provideUserRepository(
        userDao: UserDao,
        dataStore: DataStore<Preferences>
    ): UserRepository {
        return UserRepository(userDao, dataStore)
    }

    @Provides
    @Singleton
    fun provideScanRepository(
        scanDao: ScanDao,
        syncQueueDao: SyncQueueDao
    ): ScanRepository {
        return ScanRepository(scanDao, syncQueueDao)
    }

    @Provides
    @Singleton
    fun provideTreatmentRepository(
        treatmentDao: TreatmentDao,
        diseaseDao: DiseaseDao
    ): TreatmentRepository {
        return TreatmentRepository(treatmentDao, diseaseDao)
    }

    @Provides
    @Singleton
    fun provideSyncRepository(
        syncQueueDao: SyncQueueDao,
        scanDao: ScanDao,
        treatmentDao: TreatmentDao,
        escalationDao: EscalationDao,
        ratingDao: RatingDao,
        userDao: UserDao,
        notificationRepository: NotificationRepository,
        api: ShambaMedicApi,
        dataStore: DataStore<Preferences>
    ): SyncRepository {
        return SyncRepository(
            syncQueueDao,
            scanDao,
            treatmentDao,
            escalationDao,
            ratingDao,
            userDao,
            notificationRepository,
            api,
            dataStore
        )
    }

    @Provides
    @Singleton
    fun provideEscalationRepository(
        escalationDao: EscalationDao,
        scanDao: ScanDao,
        notificationRepository: NotificationRepository
    ): EscalationRepository {
        return EscalationRepository(escalationDao, scanDao, notificationRepository)
    }

    @Provides
    @Singleton
    fun provideRatingRepository(
        ratingDao: RatingDao
    ): RatingRepository {
        return RatingRepository(ratingDao)
    }

    @Provides
    @Singleton
    fun provideNotificationRepository(
        notificationDao: NotificationDao
    ): NotificationRepository {
        return NotificationRepository(notificationDao)
    }
}
