package com.app.backend.domain.chat.room.dto.response

import com.app.backend.domain.chat.room.entity.ChatRoom

data class ChatRoomListResponse(
	val chatRoomId: Long?,
	val groupId: Long,
	val groupName: String,
	val participant: Long
) {
	companion object {
		fun from(chatRoom: ChatRoom, participant: Long): ChatRoomListResponse {
			return ChatRoomListResponse(
				chatRoomId = chatRoom.id,
				groupId = chatRoom.group.id,
				groupName = chatRoom.group.name,
				participant = participant
			)
		}
	}
}
