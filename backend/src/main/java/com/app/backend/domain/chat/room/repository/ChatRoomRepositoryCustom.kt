package com.app.backend.domain.chat.room.repository

import com.app.backend.domain.chat.room.dto.response.ChatRoomDetailResponse
import com.app.backend.domain.chat.room.dto.response.ChatRoomListResponse

interface ChatRoomRepositoryCustom {
	fun findAllByMemberId(memberId: Long): List<ChatRoomListResponse>
	fun findByIdWithApprovedMembers(id: Long): ChatRoomDetailResponse?
}
