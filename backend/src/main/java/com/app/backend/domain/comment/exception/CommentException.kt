package com.app.backend.domain.comment.exception;


import com.app.backend.global.error.exception.DomainErrorCode;
import com.app.backend.global.error.exception.DomainException;

public class CommentException extends DomainException {

	public CommentException(DomainErrorCode domainErrorCode) {
		super(domainErrorCode);
	}
}

