package com.app.backend.domain.chat.message.dto.request;

import com.app.backend.domain.chat.message.entity.Message;

public record MessageRequest (Long chatRoomId, Long senderId, String senderNickname, String content){

	public Message toEntity() {
		return Message.builder()
			.chatRoomId(chatRoomId)
			.senderId(senderId)
			.senderNickname(senderNickname)
			.content(content)
			.disabled(false)
			.build();
	}
}
