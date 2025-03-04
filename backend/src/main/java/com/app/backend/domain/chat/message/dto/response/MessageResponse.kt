package com.app.backend.domain.chat.message.dto.response;

import com.app.backend.domain.chat.message.entity.Message;
import lombok.Builder;

@Builder
public record MessageResponse(String id, Long chatRoomId, Long senderId, String senderNickname, String content,
							  String createdAt) {

	public static MessageResponse from(Message message) {
		return MessageResponse.builder()
			.id(message.getId().toString())
			.chatRoomId(message.getChatRoomId())
			.senderId(message.getSenderId())
			.senderNickname(message.getSenderNickname())
			.content(message.getContent())
			.createdAt(message.getCreatedAt().toString())
			.build();
	}
}
