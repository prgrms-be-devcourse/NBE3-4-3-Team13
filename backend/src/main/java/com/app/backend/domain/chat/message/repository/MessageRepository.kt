package com.app.backend.domain.chat.message.repository;

import org.bson.types.ObjectId
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.mongodb.repository.MongoRepository
import com.app.backend.domain.chat.message.entity.Message

interface MessageRepository : MongoRepository<Message, ObjectId> {
	fun findByChatRoomIdAndDisabledFalse(chatRoomId: Long, pageable: Pageable): Page<Message>
}
