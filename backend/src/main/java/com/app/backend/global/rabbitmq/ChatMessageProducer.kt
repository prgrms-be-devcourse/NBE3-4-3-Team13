package com.app.backend.global.rabbitmq

import org.springframework.amqp.rabbit.core.RabbitTemplate
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import com.app.backend.domain.chat.message.dto.response.MessageResponse
import io.github.oshai.kotlinlogging.KotlinLogging

@Service
class ChatMessageProducer(
	private val rabbitTemplate: RabbitTemplate,
	@Value("\${rabbitmq.exchange.name}") private val exchange: String
) {
	private val log = KotlinLogging.logger {}

	fun sendMessage(message: MessageResponse, roomId: String) {
		log.info { "message send : $message" }
		rabbitTemplate.convertAndSend(exchange, roomId, message)
	}
}
