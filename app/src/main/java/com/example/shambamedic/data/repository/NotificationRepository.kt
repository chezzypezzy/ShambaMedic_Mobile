package com.example.shambamedic.data.repository

import com.example.shambamedic.data.local.dao.NotificationDao
import com.example.shambamedic.data.local.entity.NotificationEntity
import com.example.shambamedic.domain.model.Notification
import com.example.shambamedic.domain.model.toDomain
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.UUID

class NotificationRepository(
    private val notificationDao: NotificationDao
) {

    suspend fun createNotification(
        userId: String,
        title: String,
        message: String,
        relatedScanId: String? = null
    ): Result<Notification> {
        return try {
            val notification = NotificationEntity(
                notificationId = UUID.randomUUID().toString(),
                userId = userId,
                title = title,
                message = message,
                relatedScanId = relatedScanId,
                isRead = false,
                createdTimestamp = System.currentTimeMillis()
            )
            notificationDao.insertNotification(notification)
            Result.success(notification.toDomain())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun getNotifications(userId: String): Flow<List<Notification>> {
        return notificationDao.getNotificationsForUser(userId).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    fun getUnreadCount(userId: String): Flow<Int> {
        return notificationDao.getUnreadCount(userId)
    }

    suspend fun markAsRead(notificationId: String) {
        notificationDao.markAsRead(notificationId)
    }

    suspend fun markAllAsRead(userId: String) {
        notificationDao.markAllAsRead(userId)
    }
}
