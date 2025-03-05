package com.app.backend.domain.member.dto.request

import io.swagger.v3.oas.annotations.media.Schema

data class MemberModifyRequestDto(
    @Schema(
        description = "닉네임",
        example = "새로운닉네임",
        nullable = true
    )
    val nickname: String?,

    @Schema(
        description = "비밀번호",
        example = "새로운비밀번호",
        nullable = true
    )
    val password: String?
)