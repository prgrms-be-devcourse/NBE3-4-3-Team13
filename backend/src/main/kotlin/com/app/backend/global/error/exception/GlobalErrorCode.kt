package com.app.backend.global.error.exception

import org.springframework.http.HttpStatus

enum class GlobalErrorCode(
    override val status: HttpStatus,
    override val code: String,
    override val message: String
) : DomainErrorCode {
    INVALID_INPUT_VALUE(HttpStatus.BAD_REQUEST, "GL001", "올바르지 않은 입력값"),
    METHOD_NOT_ALLOWED(HttpStatus.METHOD_NOT_ALLOWED, "GL002", "올바르지 않은 HTTP 메서드"),
    ENTITY_NOT_FOUND(HttpStatus.NOT_FOUND, "GL003", "값을 찾지 못함"),
    HANDLE_ACCESS_DENIED(HttpStatus.FORBIDDEN, "GL004", "요청이 거부됨"),
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "GL005", "서버 내부 오류 발생");
}
