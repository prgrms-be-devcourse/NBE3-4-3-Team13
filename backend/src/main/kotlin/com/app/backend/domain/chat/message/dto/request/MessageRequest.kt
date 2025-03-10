package com.app.backend.domain.chat.message.dto.request

import com.app.backend.domain.chat.message.entity.Message
import java.time.LocalDateTime

data class MessageRequest(
	var id: String? = null,
	val chatRoomId: Long,
	val senderId: Long,
	val senderNickname: String,
	val content: String
) {
	constructor() : this(null,0L, 0L, "", "")

	fun toEntity(): Message {
		return Message(
			id = id.toString(),
			chatRoomId = chatRoomId,
			senderId = senderId,
			senderNickname = senderNickname,
			content = content,
			disabled = false,
			createdAt = LocalDateTime.now()
		)
	}
}
