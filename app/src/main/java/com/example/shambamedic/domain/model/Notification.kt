package com.example.shambamedic.domain.model

import com.example.shambamedic.data.local.entity.NotificationEntity

data class Notification(
    val notificationId: String,
    val userId: String,
    val title: String,
    val message: String,
    val relatedScanId: String?,
    val isRead: Boolean,
    val createdTimestamp: Long
)

fun NotificationEntity.toDomain() = Notification(
    notificationId = notificationId,
    userId = userId,
    title = title,
    message = message,
    relatedScanId = relatedScanId,
    isRead = isRead,
    createdTimestamp = createdTimestamp
)

fun Notification.toEntity() = NotificationEntity(
    notificationId = notificationId,
    userId = userId,
    title = title,
    message = message,
    relatedScanId = relatedScanId,
    isRead = isRead,
    createdTimestamp = createdTimestamp
)
