package com.app.backend.domain.chat.message.controller;

import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.app.backend.domain.chat.message.dto.response.MessageResponse;
import com.app.backend.domain.chat.message.dto.response.ResponseMessage;
import com.app.backend.domain.chat.message.service.MessageService;
import com.app.backend.global.annotation.CustomPageJsonSerializer;
import com.app.backend.global.dto.response.ApiResponse;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/chatrooms")
@RequiredArgsConstructor
public class MessageController {

	private final MessageService messageService;

	@GetMapping("/{id}/messages")
	@CustomPageJsonSerializer(hasContent = false,
							  numberOfElements = false,
							  size = false,
							  number = false,
							  hasPrevious = false,
							  isFirst = false,
							  sort = false,
							  empty = false)
	public ApiResponse<Page<MessageResponse>> getMessagesByChatRoomId(@PathVariable Long id,
		@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int size) {
		Page<MessageResponse> messages = messageService.getMessagesByChatRoomId(id, page, size);
		return ApiResponse.of(true, HttpStatus.OK, ResponseMessage.READ_CHAT_MESSAGES_SUCCESS.getMessage(), messages);
	}
}
