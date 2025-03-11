package com.app.backend.domain.chat.message

import com.app.backend.domain.chat.message.entity.Message
import java.awt.SystemColor.text
import java.time.LocalDateTime
import java.util.*

open class MessageUtil {
    fun createMessage(
        chatRoomId: Long,
        senderId: Long,
        senderNickname: String,
        content: String,
        createdAt: LocalDateTime = LocalDateTime.now()
    ): Message {
        return Message(
            id = UUID.randomUUID().toString(),
            chatRoomId = chatRoomId,
            senderId = senderId,
            senderNickname = senderNickname,
            content = content,
            createdAt = createdAt,
            type = "text",
            disabled = false
        )
    }
}