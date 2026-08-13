package com.example.shambamedic.domain.usecase

import com.example.shambamedic.data.repository.SyncRepository
import javax.inject.Inject

class SyncScansUseCase @Inject constructor(
    private val syncRepository: SyncRepository
) {
    suspend operator fun invoke(): Result<Int> {
        return syncRepository.syncPendingScans()
    }
}
