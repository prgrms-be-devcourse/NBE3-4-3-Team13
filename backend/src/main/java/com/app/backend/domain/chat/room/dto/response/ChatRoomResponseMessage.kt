package com.app.backend.domain.chat.room.dto.response

enum class ChatRoomResponseMessage(val message: String) {
	READ_CHAT_ROOMS_SUCCESS("채팅방 목록 조회 성공"),
	READ_CHAT_ROOM_SUCCESS("채팅방 상세 조회 성공")
}
