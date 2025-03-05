package com.app.backend.domain.chat.message.controller

import org.springframework.messaging.handler.annotation.MessageMapping
import org.springframework.messaging.handler.annotation.Payload
import org.springframework.stereotype.Controller
import com.app.backend.domain.chat.message.dto.request.MessageRequest
import com.app.backend.domain.chat.message.service.MessageService
import com.app.backend.global.rabbitmq.ChatMessageProducer

@Controller
class MessageWebSocketController(
	private val messageService: MessageService,
	private val chatMessageProducer: ChatMessageProducer
) {
	@MessageMapping("chat.{chatRoomId}")
	fun sendMessage(@Payload messageRequest: MessageRequest) {
		val messageResponse = messageService.saveMessage(messageRequest)
		chatMessageProducer.sendMessage(messageResponse, "chatroom." + messageRequest.chatRoomId)
	}
}