package com.example.shambamedic.domain.usecase

import com.example.shambamedic.data.repository.ScanRepository
import com.example.shambamedic.domain.model.Scan
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetScanHistoryUseCase @Inject constructor(
    private val scanRepository: ScanRepository
) {
    operator fun invoke(userId: String): Flow<List<Scan>> {
        return scanRepository.getScanHistory(userId)
    }
}
