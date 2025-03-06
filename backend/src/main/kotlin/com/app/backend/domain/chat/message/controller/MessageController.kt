package com.app.backend.domain.chat.message.controller

import org.springframework.data.domain.Page
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*
import com.app.backend.domain.chat.message.dto.response.MessageResponse
import com.app.backend.domain.chat.message.dto.response.ResponseMessage
import com.app.backend.domain.chat.message.service.MessageService
import com.app.backend.global.annotation.CustomPageJsonSerializer
import com.app.backend.global.dto.response.ApiResponse

@RestController
@RequestMapping("/api/v1/chatrooms")
class MessageController(
	private val messageService: MessageService
) {
	@GetMapping("/{id}/messages")
	@CustomPageJsonSerializer(
		hasContent = false,
		numberOfElements = false,
		size = false,
		number = false,
		hasPrevious = false,
		isFirst = false,
		sort = false,
		empty = false
	)
	fun getMessagesByChatRoomId(
		@PathVariable id: Long,
		@RequestParam(defaultValue = "0") page: Int,
		@RequestParam(defaultValue = "20") size: Int
	): ApiResponse<Page<MessageResponse>> {
		val messages = messageService.getMessagesByChatRoomId(id, page, size)
		return ApiResponse.of(true, HttpStatus.OK, ResponseMessage.READ_CHAT_MESSAGES_SUCCESS.message, messages)
	}
}
