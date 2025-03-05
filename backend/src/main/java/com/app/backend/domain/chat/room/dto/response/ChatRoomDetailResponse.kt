package com.app.backend.domain.chat.room.dto.response

import com.app.backend.domain.group.dto.response.GroupChatResponse
import com.app.backend.domain.member.dto.response.MemberChatResponseDto

data class ChatRoomDetailResponse(
	val chatRoomId: Long,
	val group: GroupChatResponse,
) {
	var members: List<MemberChatResponseDto> = emptyList()
		private set

	fun addMembers(members: List<MemberChatResponseDto>) {
		this.members = members
	}
}
