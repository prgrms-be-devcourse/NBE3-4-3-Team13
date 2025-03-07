package com.app.backend.domain.chat.util

import com.app.backend.domain.chat.room.dto.response.ChatRoomListResponse

open class Utils {
    fun createChatRoomResponse(
        chatRoomId: Long,
        groupId: Long,
        groupName: String,
        participant: Long
    ): ChatRoomListResponse {
        return ChatRoomListResponse(
            chatRoomId = chatRoomId,
            groupId = groupId,
            groupName = groupName,
            participant = participant
        )
    }
}