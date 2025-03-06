package com.app.backend.domain.notification.entity

import com.app.backend.domain.notification.dto.NotificationEvent
import com.app.backend.global.entity.BaseEntity
import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
class Notification(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    val userId: String,
    val title: String,
    val content: String,
    var isRead: Boolean = false,

    @Enumerated(EnumType.STRING)
    val type: NotificationEvent.NotificationType,

    val targetId: Long,
    val createdAt: LocalDateTime
) : BaseEntity() {

    fun markAsRead() {
        isRead = true
    }

    companion object {
        fun create(
            userId: String,
            title: String,
            content: String,
            type: NotificationEvent.NotificationType,
            targetId: Long,
            createdAt: LocalDateTime = LocalDateTime.now()
        ) = Notification(
            userId = userId,
            title = title,
            content = content,
            type = type,
            targetId = targetId,
            createdAt = createdAt
        )
    }
}