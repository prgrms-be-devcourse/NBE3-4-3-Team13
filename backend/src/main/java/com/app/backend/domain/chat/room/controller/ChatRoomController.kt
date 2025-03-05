package com.app.backend.domain.chat.room.controller

import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import com.app.backend.domain.chat.room.dto.response.ChatRoomDetailResponse
import com.app.backend.domain.chat.room.dto.response.ChatRoomResponseMessage
import com.app.backend.domain.chat.room.service.ChatRoomService
import com.app.backend.global.dto.response.ApiResponse

@RestController
@RequestMapping("/api/v1/chatrooms/")
class ChatRoomController(
    private val chatRoomService: ChatRoomService
) {

    @GetMapping("/{id}")
    fun getChatRoom(@PathVariable id: Long): ApiResponse<ChatRoomDetailResponse> {
        val chatRoomDetails = chatRoomService.getChatRoomDetailsWithApprovedMembers(id)
        return ApiResponse.of(
            true,
            HttpStatus.OK,
            ChatRoomResponseMessage.READ_CHAT_ROOM_SUCCESS.message,
            chatRoomDetails
        )
    }
}