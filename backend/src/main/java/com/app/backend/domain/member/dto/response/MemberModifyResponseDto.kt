package com.app.backend.domain.member.dto.response

import com.app.backend.domain.member.entity.Member

data class MemberModifyResponseDto(
    val id: Long,
    val username: String,
    val password: String,
    val nickname: String,
    val role: String,
    val disabled: Boolean
) {
    companion object {
        fun of(member: Member) = MemberModifyResponseDto(
            id = member.id ?: throw IllegalArgumentException("Member ID cannot be null"),
            username = member.username ?: "",
            nickname = member.nickname ?: "",
            role = member.role ?: "",
            disabled = member.disabled,
            password = member.password ?: ""
        )
    }
}