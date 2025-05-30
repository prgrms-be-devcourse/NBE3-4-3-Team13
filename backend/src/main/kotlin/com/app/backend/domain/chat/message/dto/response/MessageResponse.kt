package com.app.backend.domain.chat.message.dto.response

import com.app.backend.domain.chat.message.dto.request.MessageRequest
import com.app.backend.domain.chat.message.entity.Message
import java.time.LocalDateTime

data class MessageResponse(
	val id: String,
	val chatRoomId: Long,
	val senderId: Long,
	val senderNickname: String,
	val content: String,
	val createdAt: String
) {
	companion object {
		fun from(message: Message): MessageResponse {
			return MessageResponse(
				id = message.id,
				chatRoomId = message.chatRoomId,
				senderId = message.senderId,
				senderNickname = message.senderNickname,
				content = message.content,
				createdAt = message.createdAt.toString()
			)
		}

		fun from(messageRequest: MessageRequest): MessageResponse {
			return MessageResponse(
				id = messageRequest.id.toString(),
				chatRoomId = messageRequest.chatRoomId,
				senderId = messageRequest.senderId,
				senderNickname = messageRequest.senderNickname,
				content = messageRequest.content,
				createdAt = LocalDateTime.now().toString()
			)
		}
	}
}
