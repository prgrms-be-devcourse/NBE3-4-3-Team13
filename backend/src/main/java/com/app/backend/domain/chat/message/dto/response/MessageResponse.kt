package com.app.backend.domain.chat.message.dto.response;

import com.app.backend.domain.chat.message.entity.Message

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
				id = message.id.toString(),
				chatRoomId = message.chatRoomId,
				senderId = message.senderId,
				senderNickname = message.senderNickname,
				content = message.content,
				createdAt = message.createdAt.toString()
			)
		}
	}
}
