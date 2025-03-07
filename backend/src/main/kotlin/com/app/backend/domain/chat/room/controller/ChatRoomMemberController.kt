package com.app.backend.domain.chat.room.controller

import org.springframework.http.HttpStatus
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import com.app.backend.domain.chat.room.dto.response.ChatRoomListResponse
import com.app.backend.domain.chat.room.dto.response.ChatRoomResponseMessage
import com.app.backend.domain.chat.room.service.ChatRoomService
import com.app.backend.domain.member.entity.MemberDetails
import com.app.backend.global.dto.response.ApiResponse

@RestController
@RequestMapping("/api/v1/members")
class ChatRoomMemberController (
    private val chatRoomService: ChatRoomService
) {

    @GetMapping("/chatrooms")
    fun getChatRoomsByMemberId(@AuthenticationPrincipal memberDetails: MemberDetails): ApiResponse<List<ChatRoomListResponse>> {
        val chatRoomsByMemberId = memberDetails.id?.let { chatRoomService.getChatRoomsByMemberId(it) }
        return ApiResponse.of(
            true,
            HttpStatus.OK,
            ChatRoomResponseMessage.READ_CHAT_ROOMS_SUCCESS.message,
            chatRoomsByMemberId
        )
    }
}