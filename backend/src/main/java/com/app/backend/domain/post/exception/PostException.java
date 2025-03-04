package com.app.backend.domain.post.exception;

import com.app.backend.global.error.exception.DomainErrorCode;
import com.app.backend.global.error.exception.DomainException;

public class PostException extends DomainException {

    public PostException(final DomainErrorCode errorCode) {super(errorCode);}

}
