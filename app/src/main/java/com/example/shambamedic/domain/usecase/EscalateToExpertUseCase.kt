package com.example.shambamedic.domain.usecase

import com.example.shambamedic.data.repository.EscalationRepository
import com.example.shambamedic.domain.model.Escalation
import javax.inject.Inject

class EscalateToExpertUseCase @Inject constructor(
    private val escalationRepository: EscalationRepository
) {
    suspend operator fun invoke(scanId: String): Result<Escalation> {
        return escalationRepository.createEscalation(scanId)
    }
}
