package com.app.backend.domain.member.dto.kakao

data class TokenDto(
    val id: String,
    val accessToken: String,
    val refreshToken: String,
    val role: String
) {
}