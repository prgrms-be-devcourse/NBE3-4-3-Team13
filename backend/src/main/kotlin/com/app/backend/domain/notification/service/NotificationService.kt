package com.app.backend.domain.notification.service

import com.app.backend.domain.notification.dto.NotificationEvent
import com.app.backend.domain.notification.dto.NotificationMessage
import com.app.backend.domain.notification.dto.NotificationProducer
import com.app.backend.domain.notification.entity.Notification
import com.app.backend.domain.notification.repository.NotificationRepository
import org.slf4j.LoggerFactory
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

private val log = LoggerFactory.getLogger(NotificationService::class.java)

@Service
@Transactional
class NotificationService(
    private val notificationRepository: NotificationRepository,
    private val notificationProducer: NotificationProducer,
    private val kafkaTemplate: KafkaTemplate<String, NotificationMessage>
) {
    fun sendNotification(
        userId: String,
        title: String,
        content: String,
        type: NotificationEvent.NotificationType,
        targetId: Long
    ) {
        // 알림 저장
        val notification = Notification.create(
            userId = userId,
            title = title,
            content = content,
            type = type,
            targetId = targetId,
            createdAt = LocalDateTime.now()
        ).let { notificationRepository.save(it) }

        // Kafka로 메시지 전송
        val message = NotificationMessage(
            id = notification.id,
            userId = notification.userId,
            title = notification.title,
            content = notification.content,
            createdAt = notification.createdAt,
            isRead = notification.isRead
        )

        kafkaTemplate.send(NotificationEvent.NotificationType.GROUP_INVITE.toString(), userId, message)
        notificationProducer.sendNotification(message)
    }

    fun save(notification: Notification) {
        notificationRepository.save(notification)
    }

    @Transactional(readOnly = true)
    fun getNotifications(userId: String): List<NotificationMessage> {
        return notificationRepository.findByUserIdAndIsReadFalseOrderByCreatedAtDesc(userId)
            .map { notification ->
                NotificationMessage(
                    id = notification.id,
                    userId = notification.userId,
                    title = notification.title,
                    content = notification.content,
                    createdAt = notification.createdAt,
                    isRead = notification.isRead
                )
            }
    }

    fun markAsRead(notificationId: Long) {
        val notification = notificationRepository.findById(notificationId)
            .orElseThrow { RuntimeException("알림을 찾을 수 없습니다.") }
        notification.markAsRead()
        notificationRepository.save(notification)
    }
}