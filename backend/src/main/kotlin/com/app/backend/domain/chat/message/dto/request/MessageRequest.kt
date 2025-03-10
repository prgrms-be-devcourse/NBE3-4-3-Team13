package com.app.backend.domain.chat.message.dto.request

import com.app.backend.domain.chat.message.entity.Message

data class MessageRequest(
	val chatRoomId: Long,
	val senderId: Long,
	val senderNickname: String,
	val content: String
) {
	constructor() : this(0L, 0L, "", "")

	fun toEntity(): Message {
		return Message(
			chatRoomId = chatRoomId,
			senderId = senderId,
			senderNickname = senderNickname,
			content = content,
			disabled = false
		)
	}
}
