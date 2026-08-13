package com.example.shambamedic.domain.usecase

import com.example.shambamedic.data.repository.EscalationRepository
import javax.inject.Inject

// DEBUG ONLY, offline UI testing only: wraps EscalationRepository.simulateResolution().
// Using this while also testing real sync will create local data that can never match
// the backend - don't mix the two in the same test session.
class SimulateResolutionUseCase @Inject constructor(
    private val escalationRepository: EscalationRepository
) {
    suspend operator fun invoke(scanId: String, diagnosis: String): Result<Unit> {
        return escalationRepository.simulateResolution(scanId, diagnosis)
    }
}
