package com.app.backend.domain.chat.room.dto.response;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ChatRoomResponseMessage {
	READ_CHAT_ROOMS_SUCCESS("채팅방 목록 조회 성공"),
	READ_CHAT_ROOM_SUCCESS("채팅방 상세 조회 성공");

	private final String message;
}
