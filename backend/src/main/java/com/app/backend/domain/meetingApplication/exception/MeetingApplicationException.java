package com.app.backend.domain.meetingApplication.exception;

import com.app.backend.global.error.exception.DomainErrorCode;
import com.app.backend.global.error.exception.DomainException;

public class MeetingApplicationException extends DomainException {
	public MeetingApplicationException(final DomainErrorCode errorCode) {
		super(errorCode);
	}
}