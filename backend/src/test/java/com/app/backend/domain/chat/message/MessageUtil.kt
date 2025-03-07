package com.app.backend.domain.chat.message

import com.app.backend.domain.chat.message.entity.Message
import org.bson.types.ObjectId
import java.time.LocalDateTime

open class MessageUtil {
    fun createMessage(
        chatRoomId: Long,
        senderId: Long,
        senderNickname: String,
        content: String,
        createdAt: LocalDateTime = LocalDateTime.now()
    ): Message {
        return Message(
            id = ObjectId(),
            chatRoomId = chatRoomId,
            senderId = senderId,
            senderNickname = senderNickname,
            content = content,
            createdAt = createdAt,
            disabled = false
        )
    }
}