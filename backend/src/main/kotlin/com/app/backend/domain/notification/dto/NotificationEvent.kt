package com.app.backend.domain.notification.dto

data class NotificationEvent(
    val id: Long,
    val userId: String,
    val title: String,
    val content: String,
    val type: NotificationType
) {
    enum class NotificationType {
        GROUP_INVITE,
        NEW_MESSAGE,
        GROUP_UPDATE
    }
}