package com.app.backend.domain.post.exception;

import com.app.backend.global.error.exception.DomainErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum PostErrorCode implements DomainErrorCode {

    POST_NOT_FOUND(HttpStatus.NOT_FOUND,"P001", "게시물 정보가 존재하지 않음"),
    POST_UNAUTHORIZATION(HttpStatus.FORBIDDEN, "P002", "게시물 접근 권한이 없음");

    private final HttpStatus status;
    private final String code;
    private final String message;

}
