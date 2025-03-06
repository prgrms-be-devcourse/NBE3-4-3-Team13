package com.app.backend.domain.member.dto.response

import com.app.backend.domain.group.entity.GroupRole

data class MemberChatResponseDto(val memberId: Long, val memberNickname: String, val groupRole: GroupRole)
