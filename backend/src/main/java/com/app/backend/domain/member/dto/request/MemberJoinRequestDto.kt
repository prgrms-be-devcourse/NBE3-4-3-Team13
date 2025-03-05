package com.app.backend.domain.member.dto.request

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

data class MemberJoinRequestDto(
    @field: NotBlank(message = "아이디는 필수입니다.")
    val username: String,
    @field: NotBlank(message = "비밀번호는 필수입니다.")
    val password: String,
    @field: NotBlank(message = "닉네임은 필수입니다.")
    @field: Size(min = 2, max = 10, message = "닉네임은 2~10자 만들어야 합니다.")
    val nickname: String,
    val role: String
) {
}