package com.app.backend.global.error.exception;

import org.springframework.http.HttpStatus;

public interface DomainErrorCode {

    HttpStatus getStatus();

    String getCode();

    String getMessage();

}
