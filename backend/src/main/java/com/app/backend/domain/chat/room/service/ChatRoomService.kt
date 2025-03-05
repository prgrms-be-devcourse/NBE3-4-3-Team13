package com.app.backend.domain.chat.room.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.app.backend.domain.chat.room.dto.response.ChatRoomDetailResponse;
import com.app.backend.domain.chat.room.dto.response.ChatRoomListResponse;
import com.app.backend.domain.chat.room.exception.ChatRoomErrorCode;
import com.app.backend.domain.chat.room.exception.ChatRoomException;
import com.app.backend.domain.chat.room.repository.ChatRoomRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ChatRoomService {

	private final ChatRoomRepository chatRoomRepository;

	public List<ChatRoomListResponse> getChatRoomsByMemberId(Long memberId) {
		return chatRoomRepository.findAllByMemberId(memberId);
	}

	public ChatRoomDetailResponse getChatRoomDetailsWithApprovedMembers(Long chatRoomId) {
		// TODO DTO 로 매핑해서 바로 가져올 때, 필드 값으로 null 이 들어가서 NullPointerException 이 터질 가능 성이 있는 부분이 있으면 추가적으로 예외 로직 추가
		ChatRoomDetailResponse chatRoomDetailResponse = chatRoomRepository.findByIdWithApprovedMembers(chatRoomId);

		if(chatRoomDetailResponse == null) {
			throw new ChatRoomException(ChatRoomErrorCode.CHAT_ROOM_NOT_FOUND);
		}

		return chatRoomDetailResponse;
	}
}
