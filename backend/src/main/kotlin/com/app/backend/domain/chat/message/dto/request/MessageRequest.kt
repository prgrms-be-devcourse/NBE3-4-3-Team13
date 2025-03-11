package com.app.backend.domain.chat.message.dto.request

import com.app.backend.domain.chat.message.entity.Message
import java.time.LocalDateTime

data class MessageRequest(
	var id: String? = null,
	val chatRoomId: Long,
	val senderId: Long,
	val senderNickname: String,
	val content: String,
	val fileUrls: MutableList<String> = mutableListOf(),
	val type: String
) {
	constructor() : this(null,0L, 0L, "", "", mutableListOf(), "")

	fun toEntity(): Message {
		return Message(
			id = id.toString(),
			chatRoomId = chatRoomId,
			senderId = senderId,
			senderNickname = senderNickname,
			content = content,
			fileUrls = fileUrls,
			type = type,
			disabled = false,
			createdAt = LocalDateTime.now()
		)
	}
}
