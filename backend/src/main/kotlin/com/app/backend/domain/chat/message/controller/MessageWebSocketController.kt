package com.app.backend.domain.chat.message.controller

import org.springframework.messaging.handler.annotation.MessageMapping
import org.springframework.messaging.handler.annotation.Payload
import org.springframework.stereotype.Controller
import com.app.backend.domain.chat.message.dto.request.MessageRequest
import com.app.backend.domain.chat.message.dto.response.MessageResponse
import com.app.backend.domain.chat.message.service.MessageService
import com.app.backend.global.rabbitmq.ChatMessageProducer
import java.util.*

@Controller
class MessageWebSocketController(
	private val messageService: MessageService,
	private val chatMessageProducer: ChatMessageProducer
) {
	@MessageMapping("chat.{chatRoomId}")
	fun sendMessage(@Payload messageRequest: MessageRequest) {
		//1. 서버에서 UUID 생성
		val generatedId = UUID.randomUUID().toString()

		//2. 생성된 UUID를 MessageRequest에 바인딩
		val message = messageRequest.copy(id = generatedId)

		//3. 클라이언트로 보낼 MessageResponse객체로 변환
		val messageResponse = MessageResponse.from(message)

		//4. 클라이언트로 메세지 발행
		chatMessageProducer.sendMessage(messageResponse, "chatroom." + messageRequest.chatRoomId)

		//5. 메세지 저장
		messageService.saveMessageAsync(message)
	}
}