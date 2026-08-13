package com.example.shambamedic.domain.usecase

import com.example.shambamedic.data.repository.ScanRepository
import com.example.shambamedic.domain.model.Scan
import javax.inject.Inject

class SaveScanUseCase @Inject constructor(
    private val scanRepository: ScanRepository
) {
    suspend operator fun invoke(scan: Scan): Result<Scan> {
        return scanRepository.saveScan(scan)
    }
}
