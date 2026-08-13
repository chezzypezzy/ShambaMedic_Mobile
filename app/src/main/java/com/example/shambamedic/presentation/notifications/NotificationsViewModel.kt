package com.example.shambamedic.presentation.notifications

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.shambamedic.data.repository.UserRepository
import com.example.shambamedic.domain.model.Notification
import com.example.shambamedic.domain.usecase.GetNotificationsUseCase
import com.example.shambamedic.domain.usecase.MarkNotificationReadUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class NotificationsUiState(
    val isLoading: Boolean = true,
    val notifications: List<Notification> = emptyList()
)

@HiltViewModel
class NotificationsViewModel @Inject constructor(
    private val getNotificationsUseCase: GetNotificationsUseCase,
    private val markNotificationReadUseCase: MarkNotificationReadUseCase,
    private val userRepository: UserRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(NotificationsUiState())
    val uiState: StateFlow<NotificationsUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            val user = userRepository.getCurrentUser()
            if (user == null) {
                _uiState.update { it.copy(isLoading = false) }
                return@launch
            }
            getNotificationsUseCase.getNotifications(user.userId).collect { notifications ->
                _uiState.update { it.copy(notifications = notifications, isLoading = false) }
            }
        }
    }

    fun onNotificationClicked(notification: Notification) {
        viewModelScope.launch {
            markNotificationReadUseCase.markAsRead(notification.notificationId)
        }
    }

    fun markAllRead() {
        viewModelScope.launch {
            val userId = userRepository.getCurrentUser()?.userId ?: return@launch
            markNotificationReadUseCase.markAllAsRead(userId)
        }
    }
}
