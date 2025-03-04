package com.app.backend.domain.chat.room.dto.response;

import com.app.backend.domain.chat.room.entity.ChatRoom;

import lombok.Builder;

@Builder
public record ChatRoomListResponse(Long chatRoomId, Long groupId, String groupName, Long participant) {

	public static ChatRoomListResponse from(ChatRoom chatRoom, Long participant) {
		return ChatRoomListResponse.builder()
			.chatRoomId(chatRoom.getId())
			.groupId(chatRoom.getGroup().getId())
			.groupName(chatRoom.getGroup().getName())
			.participant(participant)
			.build();
	}
}
