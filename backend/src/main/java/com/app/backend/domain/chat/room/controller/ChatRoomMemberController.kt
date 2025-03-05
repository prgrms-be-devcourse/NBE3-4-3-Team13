package com.app.backend.domain.chat.room.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.app.backend.domain.chat.room.dto.response.ChatRoomListResponse;
import com.app.backend.domain.chat.room.dto.response.ChatRoomResponseMessage;
import com.app.backend.domain.chat.room.service.ChatRoomService;
import com.app.backend.domain.member.entity.MemberDetails;
import com.app.backend.global.dto.response.ApiResponse;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/members")
@RequiredArgsConstructor
public class ChatRoomMemberController {

	private final ChatRoomService chatRoomService;

	@GetMapping("/chatrooms")
	public ApiResponse<List<ChatRoomListResponse>> getChatRoomsByMemberId(@AuthenticationPrincipal MemberDetails memberDetails) {
		List<ChatRoomListResponse> chatRoomsByMemberId = chatRoomService.getChatRoomsByMemberId(memberDetails.getId());
		return ApiResponse.of(
			true,
			HttpStatus.OK,
			ChatRoomResponseMessage.READ_CHAT_ROOMS_SUCCESS.getMessage(),
			chatRoomsByMemberId
		);
	}
}
