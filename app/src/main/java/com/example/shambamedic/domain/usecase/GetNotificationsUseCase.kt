package com.example.shambamedic.domain.usecase

import com.example.shambamedic.data.repository.NotificationRepository
import com.example.shambamedic.domain.model.Notification
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetNotificationsUseCase @Inject constructor(
    private val notificationRepository: NotificationRepository
) {
    fun getNotifications(userId: String): Flow<List<Notification>> {
        return notificationRepository.getNotifications(userId)
    }

    fun getUnreadCount(userId: String): Flow<Int> {
        return notificationRepository.getUnreadCount(userId)
    }
}
