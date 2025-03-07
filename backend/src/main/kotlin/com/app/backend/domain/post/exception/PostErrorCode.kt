package com.app.backend.domain.post.exception

import com.app.backend.global.error.exception.DomainErrorCode
import org.springframework.http.HttpStatus

enum class PostErrorCode(
    override val status: HttpStatus,
    override val code: String,
    override val message: String
) : DomainErrorCode {
    POST_NOT_FOUND(HttpStatus.NOT_FOUND, "P001", "게시물 정보가 존재하지 않음"),
    POST_UNAUTHORIZATION(HttpStatus.FORBIDDEN, "P002", "게시물 접근 권한이 없음");
}
