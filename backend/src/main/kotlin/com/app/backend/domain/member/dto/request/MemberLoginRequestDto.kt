package com.app.backend.domain.member.dto.request

data class MemberLoginRequestDto(
    val username: String,
    val password: String
) {
}