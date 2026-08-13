package com.example.shambamedic.presentation.history

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.shambamedic.R
import com.example.shambamedic.data.local.dao.DiseaseDao
import com.example.shambamedic.data.local.dao.EscalationDao
import com.example.shambamedic.data.repository.ScanRepository
import com.example.shambamedic.data.repository.UserRepository
import com.example.shambamedic.domain.model.Scan
import com.example.shambamedic.presentation.common.cropDisplayName
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ScanWithDiseaseName(
    val scan: Scan,
    val diseaseName: String,
    // null = never escalated; otherwise the related Escalation's "pending"/"resolved" status.
    val escalationStatus: String?
)

data class HistoryUiState(
    val isLoading: Boolean = true,
    val scans: List<ScanWithDiseaseName> = emptyList(),
    val filteredScans: List<ScanWithDiseaseName> = emptyList(),
    val selectedFilter: String = "all",
    val error: String? = null
)

@HiltViewModel
class HistoryViewModel @Inject constructor(
    private val scanRepository: ScanRepository,
    private val userRepository: UserRepository,
    private val diseaseDao: DiseaseDao,
    private val escalationDao: EscalationDao,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _uiState = MutableStateFlow(HistoryUiState())
    val uiState: StateFlow<HistoryUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            loadHistory()
        }
    }

    private suspend fun loadHistory() {
        val user = userRepository.getCurrentUser()
        if (user == null) {
            _uiState.update { it.copy(isLoading = false, error = "Session expired") }
            return
        }

        scanRepository.getScanHistory(user.userId).collect { list ->
            val scansWithNames = list.map { scan ->
                val escalationStatus = escalationDao.getEscalationByScanId(scan.scanId)?.status
                val diseaseName = when (escalationStatus) {
                    "pending" -> context.getString(R.string.status_awaiting_expert_review)
                    "resolved" -> context.getString(R.string.status_expert_diagnosis_received)
                    else -> scan.diseaseId?.let { diseaseId ->
                        diseaseDao.getDiseaseById(diseaseId)?.diseaseName
                    } ?: context.getString(R.string.status_healthy_crop, cropDisplayName(context, scan.cropType))
                }
                ScanWithDiseaseName(scan = scan, diseaseName = diseaseName, escalationStatus = escalationStatus)
            }
            _uiState.update { it.copy(scans = scansWithNames, isLoading = false) }
            updateFilteredScans()
        }
    }

    fun applyFilter(filter: String) {
        _uiState.update { it.copy(selectedFilter = filter) }
        updateFilteredScans()
    }

    private fun updateFilteredScans() {
        val current = _uiState.value
        val filtered = when (current.selectedFilter) {
            "all" -> current.scans
            "maize" -> current.scans.filter { it.scan.cropType == "maize" }
            "potato" -> current.scans.filter { it.scan.cropType == "potato" }
            "tomato" -> current.scans.filter { it.scan.cropType == "tomato" }
            "pending" -> current.scans.filter {
                it.scan.syncStatus == "pending"
            }
            "awaiting_review" -> current.scans.filter {
                it.escalationStatus == "pending"
            }
            else -> current.scans
        }
        _uiState.update { it.copy(filteredScans = filtered) }
    }
}
