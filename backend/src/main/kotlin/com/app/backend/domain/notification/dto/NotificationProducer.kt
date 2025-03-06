package com.app.backend.domain.notification.dto

import org.springframework.kafka.core.KafkaTemplate
import org.springframework.stereotype.Service

@Service
class NotificationProducer(
    private val kafkaTemplate: KafkaTemplate<String, NotificationMessage>
) {
    companion object {
        private const val TOPIC = "notification-topic"
    }

    fun sendNotification(message: NotificationMessage) {
        kafkaTemplate.send(TOPIC, message.userId, message)
    }
}