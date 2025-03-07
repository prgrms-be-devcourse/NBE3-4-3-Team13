package com.app.backend.domain.notification

import com.app.backend.domain.notification.dto.NotificationEvent
import com.app.backend.domain.notification.dto.NotificationMessage
import com.app.backend.domain.notification.dto.NotificationProducer
import org.slf4j.LoggerFactory
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Component
import java.time.LocalDateTime

private val log = LoggerFactory.getLogger(NotificationEventListener::class.java)

@Component
class NotificationEventListener(
    private val notificationProducer: NotificationProducer
) {
    @EventListener
    fun handleNotificationEvent(event: NotificationEvent) {
        val message = NotificationMessage(
            id = event.id,
            userId = event.userId,
            title = event.title,
            content = event.content,
            createdAt = LocalDateTime.now(),
            isRead = false
        )
        notificationProducer.sendNotification(message)
    }
}