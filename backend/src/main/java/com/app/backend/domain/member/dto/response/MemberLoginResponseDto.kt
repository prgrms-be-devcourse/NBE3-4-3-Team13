package com.app.backend.domain.member.dto.response

import com.app.backend.domain.member.entity.Member

data class MemberLoginResponseDto(
    val id: Long,
    val username: String,
    val nickname: String,
    val role: String,
    val createdAt: String,
    val modifiedAt: String,
    val accessToken: String,
    val refreshToken: String
) {
    companion object {
        fun of(member: Member, accessToken: String, refreshToken: String) = MemberLoginResponseDto(
            id = member.id ?: throw IllegalArgumentException("Member ID cannot be null"),
            username = member.username ?: "",
            nickname = member.nickname ?: "",
            role = member.role ?: "",
            createdAt = member.createdAt.toString(),
            modifiedAt = member.modifiedAt.toString(),
            accessToken = accessToken,
            refreshToken = refreshToken
        )
    }
}