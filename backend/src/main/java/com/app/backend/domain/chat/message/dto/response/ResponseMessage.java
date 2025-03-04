package com.app.backend.domain.chat.message.dto.response;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ResponseMessage {
	READ_CHAT_MESSAGES_SUCCESS("메세지 조회 성공");

	private final String message;
}
