package com.example.shambamedic.util

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.shambamedic.data.repository.SyncRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

/**
 * Automatic sync: runs SyncRepository.syncAll() (push scans -> push escalations -> push
 * ratings -> poll resolved escalations, in that order) on a periodic WorkManager schedule.
 * The manual "Sync Now" trigger on HomeScreen calls SyncManager.syncNow() directly instead -
 * see SyncManager for why.
 */
@HiltWorker
class SyncWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val syncRepository: SyncRepository
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val result = syncRepository.syncAll()
        // syncAll()'s own steps already leave failed items in "pending"/"failed" state
        // rather than throwing, so this practically never hits the retry branch - but if
        // an unexpected exception does escape, ask WorkManager to retry rather than give up.
        return if (result.isSuccess) Result.success() else Result.retry()
    }

    companion object {
        const val WORK_NAME = "shambamedic_periodic_sync"
    }
}
