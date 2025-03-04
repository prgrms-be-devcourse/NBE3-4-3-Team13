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

		ChatRoomListResponse chatRoom1 = ChatRoomListResponse.builder()
			.chatRoomId(1L)
			.groupId(1L)
			.groupName("Group 1")
			.participant(10L) // 가상의 참여 인원 수 추가
			.build();

		ChatRoomListResponse chatRoom2 = ChatRoomListResponse.builder()
			.chatRoomId(2L)
			.groupId(3L)
			.groupName("Group 2")
			.participant(5L) // 가상의 참여 인원 수 추가
			.build();

		List<ChatRoomListResponse> chatRooms = List.of(chatRoom1, chatRoom2);
		when(chatRoomRepository.findAllByMemberId(any(Long.class))).thenReturn(chatRooms);

		//when
		List<ChatRoomListResponse> result = chatRoomService.getChatRoomsByMemberId(MemberId);

		//then
		assertThat(result).isNotNull();
		assertThat(result.size()).isEqualTo(2);
		assertThat(result.get(0).chatRoomId()).isEqualTo(chatRoom1.chatRoomId());
		assertThat(result.get(0).groupId()).isEqualTo(chatRoom1.groupId());
		assertThat(result.get(0).groupName()).isEqualTo(chatRoom1.groupName());
		assertThat(result.get(0).participant()).isEqualTo(chatRoom1.participant());
		assertThat(result.get(1).chatRoomId()).isEqualTo(chatRoom2.chatRoomId());
		assertThat(result.get(1).groupId()).isEqualTo(chatRoom2.groupId());
		assertThat(result.get(1).groupName()).isEqualTo(chatRoom2.groupName());
		assertThat(result.get(1).participant()).isEqualTo(chatRoom2.participant());
	}
}