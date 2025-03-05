package com.app.backend.domain.member.dto.response

import com.app.backend.domain.member.entity.Member
import java.time.LocalDateTime

data class MemberJoinResponseDto(
    val id: Long,
    val username: String,
    val nickname: String,
    val role: String,
    val createdAt: LocalDateTime
) {
    companion object {
        @JvmStatic
        fun from(member: Member) = MemberJoinResponseDto(
            id = member.id ?: throw IllegalArgumentException("Member ID cannot be null"),
            username = member.username ?: "",
            nickname = member.nickname ?: "",
            role = member.role ?: "",
            createdAt = member.createdAt
        )
    }
}