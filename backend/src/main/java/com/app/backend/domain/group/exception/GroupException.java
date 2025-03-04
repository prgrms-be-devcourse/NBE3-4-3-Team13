package com.app.backend.domain.group.exception;

import com.app.backend.global.error.exception.DomainErrorCode;
import com.app.backend.global.error.exception.DomainException;

public class GroupException extends DomainException {
    public GroupException(final DomainErrorCode domainErrorCode) {
        super(domainErrorCode);
    }
}
