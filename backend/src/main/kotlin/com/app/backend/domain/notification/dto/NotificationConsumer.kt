package com.app.backend.domain.notification.dto

import com.app.backend.domain.notification.SseEmitters
import org.slf4j.LoggerFactory
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.stereotype.Service

private val log = LoggerFactory.getLogger(NotificationConsumer::class.java)

@Service
class NotificationConsumer(
    private val sseEmitters: SseEmitters
) {
    @KafkaListener(topics = ["notification-topic"], groupId = "notification-group")
    fun consume(message: NotificationMessage) {
        try {
            log.info("Kafka message received: {}", message)
            // SSE를 통해 클라이언트에게 알림 전송
            sseEmitters.sendToUser(message.userId, message)
            log.info("Notification sent via SSE: {}", message)
        } catch (e: Exception) {
            log.error("Failed to send notification: {}", e.message, e)
        }
    }
}