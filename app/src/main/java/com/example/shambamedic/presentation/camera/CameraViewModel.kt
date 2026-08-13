package com.example.shambamedic.presentation.camera

import android.graphics.Bitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.shambamedic.data.repository.UserRepository
import com.example.shambamedic.domain.model.ClassificationResult
import com.example.shambamedic.domain.model.Scan
import com.example.shambamedic.domain.usecase.ClassifyDiseaseUseCase
import com.example.shambamedic.domain.usecase.EscalateToExpertUseCase
import com.example.shambamedic.domain.usecase.SaveScanUseCase
import com.example.shambamedic.ml.ModelLabels
import com.example.shambamedic.util.Constants
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

data class CameraUiState(
    val isLoading: Boolean = false,
    val cropType: String = "maize",
    val capturedBitmap: Bitmap? = null,
    val classificationResult: ClassificationResult? = null,
    val savedScanId: String? = null,
    val error: String? = null,
    val hasPermission: Boolean = false,
    val showRetake: Boolean = false,
    val attemptCount: Int = 0,
    val isEscalated: Boolean = false,
    val escalationId: String? = null
)

@HiltViewModel
class CameraViewModel @Inject constructor(
    private val classifyDiseaseUseCase: ClassifyDiseaseUseCase,
    private val saveScanUseCase: SaveScanUseCase,
    private val escalateToExpertUseCase: EscalateToExpertUseCase,
    private val userRepository: UserRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(CameraUiState())
    val uiState: StateFlow<CameraUiState> = _uiState.asStateFlow()

    fun setCropType(cropType: String) {
        _uiState.update { it.copy(cropType = cropType) }
    }

    fun onPermissionResult(granted: Boolean) {
        _uiState.update { it.copy(hasPermission = granted) }
    }

    fun onImageCaptured(bitmap: Bitmap) {
        if (_uiState.value.isLoading) return
        android.util.Log.d("CameraViewModel", "Captured bitmap: ${bitmap.width}x${bitmap.height}")
        _uiState.update { it.copy(capturedBitmap = bitmap, isLoading = true, error = null) }

        viewModelScope.launch(Dispatchers.Default) {
            val result = classifyDiseaseUseCase(bitmap, _uiState.value.cropType)

            result.onSuccess { classification ->
                val isLowConfidence = classification.confidenceScore < Constants.MIN_CONFIDENCE_THRESHOLD

                if (isLowConfidence && _uiState.value.attemptCount >= 1) {
                    // Second consecutive low-confidence attempt on this leaf: escalate instead of retaking again.
                    escalateForReview(classification)
                } else {
                    _uiState.update { it.copy(
                        classificationResult = classification,
                        attemptCount = if (isLowConfidence) 1 else 0,
                        showRetake = true,
                        isLoading = false
                    ) }
                    saveScan(classification)
                }
            }.onFailure { exception ->
                _uiState.update { it.copy(
                    error = exception.message ?: "Classification failed. Please retake.",
                    showRetake = true,
                    isLoading = false
                ) }
            }
        }
    }

    private suspend fun saveScan(result: ClassificationResult) {
        val user = userRepository.getCurrentUser()
        if (user == null) {
            _uiState.update { it.copy(error = "User session not found") }
            return
        }

        val scanId = UUID.randomUUID().toString()
        val scan = Scan(
            scanId = scanId,
            userId = user.userId,
            diseaseId = ModelLabels.LABEL_TO_DISEASE_ID[result.rawLabel],
            cropType = _uiState.value.cropType,
            imageThumbnail = null, // Optional for now
            confidenceScore = result.confidenceScore,
            severity = result.severity,
            latitude = null,
            longitude = null,
            scanTimestamp = System.currentTimeMillis(),
            syncStatus = "pending"
        )

        val saveResult = saveScanUseCase(scan)
        saveResult.onSuccess {
            _uiState.update { it.copy(savedScanId = it.savedScanId ?: scan.scanId) }
        }.onFailure {
            _uiState.update { it.copy(error = "Failed to save scan") }
        }
    }

    private suspend fun escalateForReview(result: ClassificationResult) {
        val user = userRepository.getCurrentUser()
        if (user == null) {
            _uiState.update { it.copy(error = "User session not found", isLoading = false) }
            return
        }

        val scan = Scan(
            scanId = UUID.randomUUID().toString(),
            userId = user.userId,
            diseaseId = null, // unresolved until botanist responds
            cropType = _uiState.value.cropType,
            imageThumbnail = null,
            confidenceScore = result.confidenceScore,
            // Set once at scan-creation time from the model's own classification and never
            // overwritten again; the escalation workflow state lives on Escalation.status,
            // not here.
            severity = result.severity,
            latitude = null,
            longitude = null,
            scanTimestamp = System.currentTimeMillis(),
            syncStatus = "pending"
        )

        val saveResult = saveScanUseCase(scan)
        if (saveResult.isSuccess) {
            val savedScan = saveResult.getOrNull()!!
            val escalationResult = escalateToExpertUseCase(savedScan.scanId)
            if (escalationResult.isSuccess) {
                _uiState.update { it.copy(
                    isEscalated = true,
                    escalationId = escalationResult.getOrNull()?.escalationId,
                    savedScanId = savedScan.scanId,
                    isLoading = false
                ) }
            } else {
                _uiState.update { it.copy(
                    error = "Failed to escalate. Please try again.",
                    isLoading = false
                ) }
            }
        } else {
            _uiState.update { it.copy(
                error = "Failed to save scan for review.",
                isLoading = false
            ) }
        }
    }

    fun retake() {
        val wasEscalated = _uiState.value.isEscalated
        _uiState.update {
            it.copy(
                capturedBitmap = null,
                classificationResult = null,
                savedScanId = null,
                error = null,
                showRetake = false,
                // A fresh scan after escalation starts over completely; a retake on the
                // same leaf must remember this is now a repeat attempt.
                attemptCount = if (wasEscalated) 0 else it.attemptCount,
                isEscalated = if (wasEscalated) false else it.isEscalated,
                escalationId = if (wasEscalated) null else it.escalationId
            )
        }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
}
