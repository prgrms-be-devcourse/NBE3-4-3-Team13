package com.app.backend.domain.chat.room.dto.response;

import java.util.List;

import com.app.backend.domain.group.dto.response.GroupChatResponse;
import com.app.backend.domain.member.dto.response.MemberChatResponseDto;

import lombok.Getter;

@Getter
public class ChatRoomDetailResponse {

	private final Long chatRoomId;

	private final GroupChatResponse group;

	private List<MemberChatResponseDto> members;

	// QueryDSL에서 사용하는 생성자 추가
	public ChatRoomDetailResponse(Long chatRoomId, GroupChatResponse group) {
		this.chatRoomId = chatRoomId;
		this.group = group;
	}

	public void addMembers(List<MemberChatResponseDto> members) {
		this.members = members;
	}
}
