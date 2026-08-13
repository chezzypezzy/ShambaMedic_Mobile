package com.example.shambamedic.domain.usecase

import com.example.shambamedic.data.repository.NotificationRepository
import javax.inject.Inject

class MarkNotificationReadUseCase @Inject constructor(
    private val notificationRepository: NotificationRepository
) {
    suspend fun markAsRead(notificationId: String) {
        notificationRepository.markAsRead(notificationId)
    }

    suspend fun markAllAsRead(userId: String) {
        notificationRepository.markAllAsRead(userId)
    }
}
