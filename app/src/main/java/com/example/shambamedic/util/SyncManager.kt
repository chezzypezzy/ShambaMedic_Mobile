package com.example.shambamedic.util

import com.example.shambamedic.data.repository.SyncRepository
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import javax.inject.Inject
import javax.inject.Singleton

sealed class SyncResult {
    object Loading : SyncResult()
    data class Success(val syncedCount: Int) : SyncResult()
    data class Error(val message: String) : SyncResult()
}

/**
 * Manual "Sync Now" trigger (HomeScreen) only. Automatic/periodic sync is handled by
 * SyncWorker on a WorkManager schedule (see ShambaMedicApplication) instead of an
 * event-driven connectivity callback here, so it survives process death.
 */
@Singleton
class SyncManager @Inject constructor(
    private val syncRepository: SyncRepository
) {

    fun syncNow(): Flow<SyncResult> = callbackFlow {
        send(SyncResult.Loading)
        try {
            val result = syncRepository.syncAll()
            result.onSuccess { count ->
                send(SyncResult.Success(count))
            }.onFailure { error ->
                send(SyncResult.Error(error.message ?: "Sync failed"))
            }
        } catch (e: Exception) {
            send(SyncResult.Error(e.message ?: "Sync failed"))
        }
        close()
        awaitClose { }
    }
}
