package com.example.shambamedic.domain.usecase

import com.example.shambamedic.data.repository.TreatmentRepository
import com.example.shambamedic.domain.model.Treatment
import javax.inject.Inject

class GetTreatmentUseCase @Inject constructor(
    private val treatmentRepository: TreatmentRepository
) {
    suspend operator fun invoke(diseaseId: String): List<Treatment> {
        return treatmentRepository.getTreatmentsForDisease(diseaseId)
    }
}
