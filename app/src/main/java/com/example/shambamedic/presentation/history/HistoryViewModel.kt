package com.example.shambamedic.presentation.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.shambamedic.data.local.dao.DiseaseDao
import com.example.shambamedic.data.local.dao.EscalationDao
import com.example.shambamedic.data.repository.ScanRepository
import com.example.shambamedic.data.repository.UserRepository
import com.example.shambamedic.domain.model.Scan
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ScanWithDiseaseName(
    val scan: Scan,
    // The specific disease name from the DB (locale-independent, diagnosis content is
    // English-only by design) - null when the scan has no disease record, i.e. it's
    // either healthy or its status is a placeholder the UI resolves from
    // [escalationStatus] via stringResource() so it stays locale-reactive.
    val diseaseName: String?,
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
    private val escalationDao: EscalationDao
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

        combine(
            scanRepository.getScanHistory(user.userId),
            escalationDao.getAllEscalationsForUser(user.userId)
        ) { scans, escalations ->
            val escalationByScanId = escalations.associateBy { it.scanId }
            scans.map { scan ->
                val escalationStatus = escalationByScanId[scan.scanId]?.status
                val diseaseName = scan.diseaseId?.let { diseaseId ->
                    diseaseDao.getDiseaseById(diseaseId)?.diseaseName
                }
                ScanWithDiseaseName(scan = scan, diseaseName = diseaseName, escalationStatus = escalationStatus)
            }
        }.collect { scansWithNames ->
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
            "diagnosis_received" -> current.scans.filter {
                it.escalationStatus == "resolved"
            }
            else -> current.scans
        }
        _uiState.update { it.copy(filteredScans = filtered) }
    }
}
