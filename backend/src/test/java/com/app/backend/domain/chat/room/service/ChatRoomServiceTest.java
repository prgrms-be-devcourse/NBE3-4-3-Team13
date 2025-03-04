package com.app.backend.domain.chat.room.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.app.backend.domain.chat.util.Utils;
import com.app.backend.domain.chat.room.dto.response.ChatRoomListResponse;
import com.app.backend.domain.chat.room.repository.ChatRoomRepository;

@ExtendWith(MockitoExtension.class)
class ChatRoomServiceTest {

	@Mock
	private ChatRoomRepository chatRoomRepository;

	@InjectMocks
	private ChatRoomService chatRoomService;

	@Test
	@DisplayName("[성공] 채팅방 목록 조회")
	void getChatRoomsByMemberId() {
		//given
		Long MemberId = 1L;

		Utils utils = new Utils();
		ChatRoomListResponse chatRoom1 = utils.createChatRoomResponse(1L, 1L, "Group 1", 10L);
		ChatRoomListResponse chatRoom2 = utils.createChatRoomResponse(2L, 3L, "Group 2", 5L);

		List<ChatRoomListResponse> chatRooms = List.of(chatRoom1, chatRoom2);
		when(chatRoomRepository.findAllByMemberId(any(Long.class))).thenReturn(chatRooms);

		//when
		List<ChatRoomListResponse> result = chatRoomService.getChatRoomsByMemberId(MemberId);

		//then
		assertThat(result).isNotNull();
		assertThat(result.size()).isEqualTo(2);
		assertThat(result.get(0).getChatRoomId()).isEqualTo(chatRoom1.getChatRoomId());
		assertThat(result.get(0).getGroupId()).isEqualTo(chatRoom1.getGroupId());
		assertThat(result.get(0).getGroupName()).isEqualTo(chatRoom1.getGroupName());
		assertThat(result.get(0).getParticipant()).isEqualTo(chatRoom1.getParticipant());
		assertThat(result.get(1).getChatRoomId()).isEqualTo(chatRoom2.getChatRoomId());
		assertThat(result.get(1).getGroupId()).isEqualTo(chatRoom2.getGroupId());
		assertThat(result.get(1).getGroupName()).isEqualTo(chatRoom2.getGroupName());
		assertThat(result.get(1).getParticipant()).isEqualTo(chatRoom2.getParticipant());
	}
}