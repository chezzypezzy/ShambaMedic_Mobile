package com.example.shambamedic.presentation.results

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.shambamedic.BuildConfig
import com.example.shambamedic.data.repository.EscalationRepository
import com.example.shambamedic.data.repository.RatingRepository
import com.example.shambamedic.data.repository.ScanRepository
import com.example.shambamedic.data.repository.TreatmentRepository
import com.example.shambamedic.data.repository.UserRepository
import com.example.shambamedic.domain.model.Disease
import com.example.shambamedic.domain.model.Escalation
import com.example.shambamedic.domain.model.Rating
import com.example.shambamedic.domain.model.Scan
import com.example.shambamedic.domain.model.Treatment
import com.example.shambamedic.domain.usecase.SimulateResolutionUseCase
import com.example.shambamedic.domain.usecase.SubmitRatingUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ResultsUiState(
    val isLoading: Boolean = true,
    val scan: Scan? = null,
    val disease: Disease? = null,
    val treatments: List<Treatment> = emptyList(),
    val error: String? = null,
    val syncStatus: String = "pending",
    val escalation: Escalation? = null,
    val existingRating: Rating? = null,
    val showRatingPrompt: Boolean = false
)

@HiltViewModel
class ResultsViewModel @Inject constructor(
    private val scanRepository: ScanRepository,
    private val treatmentRepository: TreatmentRepository,
    private val escalationRepository: EscalationRepository,
    private val ratingRepository: RatingRepository,
    private val userRepository: UserRepository,
    private val submitRatingUseCase: SubmitRatingUseCase,
    private val simulateResolutionUseCase: SimulateResolutionUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(ResultsUiState())
    val uiState: StateFlow<ResultsUiState> = _uiState.asStateFlow()

    private var currentScanId: String = ""

    fun loadResults(scanId: String) {
        currentScanId = scanId
        viewModelScope.launch {
            if (scanId.isBlank()) {
                _uiState.update { it.copy(isLoading = false, error = "Invalid scan ID") }
                return@launch
            }

            val scan = scanRepository.getScanById(scanId)
            if (scan == null) {
                _uiState.update { it.copy(isLoading = false, error = "Scan not found") }
                return@launch
            }

            _uiState.update { it.copy(scan = scan, syncStatus = scan.syncStatus) }

            // Whether a scan was escalated for expert review is tracked on the Escalation
            // row's own status, independently of scan.severity (which now always holds the
            // original low/moderate/severe classification, never a workflow state).
            val escalation = escalationRepository.getEscalationForScan(scanId)
            _uiState.update { it.copy(escalation = escalation) }

            if (escalation != null) {
                if (escalation.status == "resolved" && escalation.botanistDiagnosis != null) {
                    val existingRating = ratingRepository.getRatingForEscalation(escalation.escalationId)
                    _uiState.update {
                        it.copy(
                            existingRating = existingRating,
                            showRatingPrompt = existingRating == null
                        )
                    }
                }
                // status == "pending": existing pending-review display behavior is unchanged.
            } else {
                scan.diseaseId?.let { diseaseId ->
                    val disease = treatmentRepository.getDiseaseById(diseaseId)
                    val treatments = treatmentRepository.getTreatmentsForDisease(diseaseId)
                    _uiState.update { it.copy(disease = disease, treatments = treatments) }
                }
            }

            _uiState.update { it.copy(isLoading = false) }
        }
    }

    fun submitRating(starRating: Int, comment: String?) {
        viewModelScope.launch {
            val escalationId = _uiState.value.escalation?.escalationId ?: return@launch
            val userId = userRepository.getCurrentUser()?.userId ?: return@launch
            submitRatingUseCase(escalationId, userId, starRating, comment)
            _uiState.update { it.copy(showRatingPrompt = false) }
            loadResults(currentScanId)
        }
    }

    // DEBUG ONLY, offline UI testing only: fakes a botanist response for the current
    // scan's escalation. Using this while also testing real sync will create local data
    // that can never match the backend - don't mix the two in the same test session.
    fun debugSimulateBotanistResponse(diagnosis: String) {
        if (!BuildConfig.DEBUG) return
        viewModelScope.launch {
            simulateResolutionUseCase(currentScanId, diagnosis)
            loadResults(currentScanId)
        }
    }
}
