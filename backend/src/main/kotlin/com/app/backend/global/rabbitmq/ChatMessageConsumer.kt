package com.app.backend.global.rabbitmq

import org.springframework.amqp.rabbit.annotation.RabbitListener
import org.springframework.messaging.simp.SimpMessagingTemplate
import org.springframework.stereotype.Service
import com.app.backend.domain.chat.message.dto.response.MessageResponse
import io.github.oshai.kotlinlogging.KotlinLogging

@Service
class ChatMessageConsumer(
	private val messagingTemplate: SimpMessagingTemplate
) {
	private val log = KotlinLogging.logger {}

	@RabbitListener(queues = ["\${rabbitmq.queue.name}"])
	fun onMessage(messageResponse: MessageResponse) { // Queue에서 message를 구독
		try {
			log.info { "Received message: $messageResponse" }
			messagingTemplate.convertAndSend("/exchange/chat.exchange/chat.${messageResponse.chatRoomId}", messageResponse)
		} catch (e: Exception) {
			log.error { "Error processing message: ${e.message}" }
		}
	}
}
