package com.app.backend.domain.category.exception

import com.app.backend.global.error.exception.DomainErrorCode
import org.springframework.http.HttpStatus

enum class CategoryErrorCode(
    override val status: HttpStatus,
    override val code: String,
    override val message: String
) : DomainErrorCode {
    CATEGORY_NAME_REQUIRED(HttpStatus.BAD_REQUEST, "C001", "카테고리 이름은 필수입니다."),
    CATEGORY_NAME_TOO_LONG(HttpStatus.BAD_REQUEST, "C002", "카테고리 이름은 최대 10자까지 가능합니다."),
    CATEGORY_NAME_DUPLICATE(HttpStatus.BAD_REQUEST, "C003", "이미 존재하는 카테고리입니다."),
    CATEGORY_NOT_FOUND(HttpStatus.NOT_FOUND, "C004", "해당 id의 카테고리는 존재하지 않습니다.");
}
