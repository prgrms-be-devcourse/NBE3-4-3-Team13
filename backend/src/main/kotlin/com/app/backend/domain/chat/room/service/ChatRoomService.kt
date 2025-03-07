package com.app.backend.domain.chat.room.service

import com.app.backend.domain.chat.room.dto.response.ChatRoomDetailResponse
import com.app.backend.domain.chat.room.dto.response.ChatRoomListResponse
import com.app.backend.domain.chat.room.exception.ChatRoomErrorCode
import com.app.backend.domain.chat.room.exception.ChatRoomException
import com.app.backend.domain.chat.room.repository.ChatRoomRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional(readOnly = true)
class ChatRoomService(
	private val chatRoomRepository: ChatRoomRepository
) {

	fun getChatRoomsByMemberId(memberId: Long): List<ChatRoomListResponse> {
		return chatRoomRepository.findAllByMemberId(memberId)
	}

	fun getChatRoomDetailsWithApprovedMembers(chatRoomId: Long): ChatRoomDetailResponse {
		val chatRoomDetailResponse = chatRoomRepository.findByIdWithApprovedMembers(chatRoomId)

		return chatRoomDetailResponse ?: throw ChatRoomException(ChatRoomErrorCode.CHAT_ROOM_NOT_FOUND)
	}
}
