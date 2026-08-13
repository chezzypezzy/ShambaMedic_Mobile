package com.example.shambamedic.presentation.home

import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.shambamedic.data.local.dao.EscalationDao
import com.example.shambamedic.data.repository.NotificationRepository
import com.example.shambamedic.data.repository.ScanRepository
import com.example.shambamedic.data.repository.UserRepository
import com.example.shambamedic.domain.model.Scan
import com.example.shambamedic.util.SyncManager
import com.example.shambamedic.util.SyncResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class RecentScanDisplay(
    val scan: Scan,
    // null = never escalated; otherwise the related Escalation's "pending"/"resolved" status.
    val escalationStatus: String?
)

data class HomeUiState(
    val isLoading: Boolean = false,
    val userName: String = "",
    val selectedLanguage: String = "en",
    val recentScanCount: Int = 0,
    val error: String? = null,
    val isLoggedOut: Boolean = false,
    val isSyncing: Boolean = false,
    val lastSyncMessage: String? = null,
    val pendingSyncCount: Int = 0,
    val totalDiseasesFound: Int = 0,
    val healthyScansCount: Int = 0,
    val pendingReviewCount: Int = 0,
    val weeklyScanCounts: List<Int> = List(7) { 0 },
    val mostCommonDisease: String? = null,
    val lastScanTimestamp: Long? = null,
    val recentScans: List<RecentScanDisplay> = emptyList(),
    val unreadNotificationCount: Int = 0
)

data class CropItem(
    val cropType: String,
    val displayName: String,
    val swahiliName: String,
    val iconRes: Int? = null,
    val backgroundColor: Color = Color.Unspecified
)

val CROP_ITEMS = listOf(
    CropItem(
        cropType = "maize",
        displayName = "Maize",
        swahiliName = "Mahindi",
        backgroundColor = Color(0xFFE8F5E9)
    ),
    CropItem(
        cropType = "potato",
        displayName = "Potato",
        swahiliName = "Viazi",
        backgroundColor = Color(0xFFFFF8E1)
    ),
    CropItem(
        cropType = "tomato",
        displayName = "Tomato",
        swahiliName = "Nyanya",
        backgroundColor = Color(0xFFFFEBEE)
    )
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val scanRepository: ScanRepository,
    private val notificationRepository: NotificationRepository,
    private val syncManager: SyncManager,
    private val escalationDao: EscalationDao
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            loadUserData()
        }
        viewModelScope.launch {
            observeUnreadNotifications()
        }
    }

    private suspend fun observeUnreadNotifications() {
        val user = userRepository.getCurrentUser() ?: return
        notificationRepository.getUnreadCount(user.userId).collect { count ->
            _uiState.update { it.copy(unreadNotificationCount = count) }
        }
    }

    private suspend fun loadUserData() {
        val user = userRepository.getCurrentUser()
        if (user != null) {
            _uiState.update { it.copy(
                userName = user.name,
                selectedLanguage = user.languagePreference
            ) }

            val pendingScans = scanRepository.getPendingScans()
            _uiState.update { it.copy(pendingSyncCount = pendingScans.size) }

            val stats = scanRepository.getDashboardStats(user.userId)
            _uiState.update {
                it.copy(
                    totalDiseasesFound = stats.totalDiseasesFound,
                    healthyScansCount = stats.healthyScansCount,
                    pendingReviewCount = stats.pendingReviewCount,
                    weeklyScanCounts = stats.weeklyScanCounts,
                    mostCommonDisease = stats.mostCommonDisease,
                    lastScanTimestamp = stats.lastScanTimestamp
                )
            }

            val recentScans = scanRepository.getRecentScans(user.userId, limit = 3).map { scan ->
                RecentScanDisplay(
                    scan = scan,
                    escalationStatus = escalationDao.getEscalationByScanId(scan.scanId)?.status
                )
            }
            _uiState.update { it.copy(recentScans = recentScans) }

            // Live-observes scan history for the running total; this collect never
            // completes, so it must stay last in this function.
            scanRepository.getScanHistory(user.userId).collect { scans ->
                _uiState.update { it.copy(recentScanCount = scans.size) }
            }
        } else {
            _uiState.update { it.copy(error = "Session expired. Please log in again.") }
        }
    }

    fun logout() {
        viewModelScope.launch {
            userRepository.logout()
            _uiState.update { it.copy(isLoggedOut = true, isLoading = false) }
        }
    }

    fun triggerSync() {
        viewModelScope.launch {
            _uiState.update { it.copy(isSyncing = true) }
            syncManager.syncNow().collect { result ->
                when (result) {
                    is SyncResult.Loading ->
                        _uiState.update { it.copy(isSyncing = true) }
                    is SyncResult.Success -> {
                        _uiState.update {
                            it.copy(
                                isSyncing = false,
                                lastSyncMessage = "Synced ${result.syncedCount} records"
                            )
                        }
                        val pendingScans = scanRepository.getPendingScans()
                        _uiState.update { it.copy(pendingSyncCount = pendingScans.size) }
                    }
                    is SyncResult.Error ->
                        _uiState.update {
                            it.copy(
                                isSyncing = false,
                                lastSyncMessage = result.message
                            )
                        }
                }
            }
        }
    }
}
