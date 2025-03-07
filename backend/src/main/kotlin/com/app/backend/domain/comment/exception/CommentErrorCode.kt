package com.app.backend.domain.comment.exception

import com.app.backend.global.error.exception.DomainErrorCode
import org.springframework.http.HttpStatus

enum class CommentErrorCode(
    override val status: HttpStatus,
    override val code: String,
    override val message: String
) : DomainErrorCode {
    COMMENT_NOT_FOUND(HttpStatus.NOT_FOUND, "CM001", "댓글을 찾을 수 없습니다"),
    COMMENT_ACCESS_DENIED(HttpStatus.FORBIDDEN, "CM003", "댓글에 대한 권한이 없습니다"),
    COMMENT_INVALID_CONTENT(HttpStatus.BAD_REQUEST, "CM004", "댓글 내용이 유효하지 않습니다");
}