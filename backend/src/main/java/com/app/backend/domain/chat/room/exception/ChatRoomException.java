package com.app.backend.domain.chat.room.exception;

import com.app.backend.global.error.exception.DomainErrorCode;
import com.app.backend.global.error.exception.DomainException;

public class ChatRoomException extends DomainException {
	public ChatRoomException(final DomainErrorCode domainErrorCode) {
		super(domainErrorCode);
	}
}
