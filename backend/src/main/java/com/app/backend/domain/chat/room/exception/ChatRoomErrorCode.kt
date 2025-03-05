package com.app.backend.domain.chat.room.exception;

import org.springframework.http.HttpStatus;

import com.app.backend.global.error.exception.DomainErrorCode;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ChatRoomErrorCode implements DomainErrorCode {

	CHAT_ROOM_NOT_FOUND(HttpStatus.NOT_FOUND, "CH001", "채팅방을 찾지 못했습니다.");

	private final HttpStatus status;
	private final String     code;
	private final String     message;
}
