package com.example.shambamedic

import android.app.Application
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.example.shambamedic.data.repository.TreatmentRepository
import com.example.shambamedic.di.ApplicationScope
import com.example.shambamedic.util.SyncWorker
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@HiltAndroidApp
class ShambaMedicApplication : Application(), Configuration.Provider {

    @Inject
    lateinit var treatmentRepository: TreatmentRepository

    @Inject
    lateinit var hiltWorkerFactory: HiltWorkerFactory

    @Inject
    @ApplicationScope
    lateinit var applicationScope: CoroutineScope

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(hiltWorkerFactory)
            .build()

    override fun onCreate() {
        super.onCreate()

        applicationScope.launch {
            treatmentRepository.seedDiseaseData()
        }

        scheduleAutomaticSync()
    }

    // Automatic sync runs on a periodic WorkManager schedule (SyncWorker) instead of the
    // event-driven connectivity-triggered coroutine this used to be, so it survives process
    // death and doesn't require the app to be foregrounded when connectivity returns.
    private fun scheduleAutomaticSync() {
        val syncRequest = PeriodicWorkRequestBuilder<SyncWorker>(15, TimeUnit.MINUTES)
            .setConstraints(
                Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.CONNECTED)
                    .build()
            )
            .build()

        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            SyncWorker.WORK_NAME,
            ExistingPeriodicWorkPolicy.KEEP,
            syncRequest
        )
    }
}
