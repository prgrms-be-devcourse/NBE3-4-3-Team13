package com.app.backend.domain.group.exception

import com.app.backend.global.error.exception.DomainErrorCode
import org.springframework.http.HttpStatus

enum class GroupLikeErrorCode private constructor(
    override val status: HttpStatus,
    override val code: String,
    override val message: String
) : DomainErrorCode {
    GROUP_NOT_FOUND(HttpStatus.NOT_FOUND, "GRL001", "모임을 찾을 수 없습니다."),
    MEMBER_NOT_FOUND(HttpStatus.NOT_FOUND, "GRL002", "멤버를 찾을 수 없습니다."),
    ALREADY_LIKED(HttpStatus.BAD_REQUEST, "GRL003", "이미 좋아요를 누르셨습니다."),
    NOT_LIKED_YET(HttpStatus.BAD_REQUEST, "GRL004", "아직 좋아요를 누르지 않으셨습니다.");
}