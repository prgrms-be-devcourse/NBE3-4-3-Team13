package com.app.backend.domain.chat.message.entity

import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.mongodb.core.mapping.Document
import org.springframework.data.mongodb.core.mapping.Field

import org.bson.types.ObjectId
import org.springframework.data.annotation.Id
import java.time.LocalDateTime

@Document(collection = "messages")
data class Message(
	@Id
	val id: ObjectId? = null,

	@Field("chat_room_id")
	val chatRoomId: Long,

	@Field("member_id")
	val senderId: Long,

	@Field("member_nickname")
	val senderNickname: String,

	@Field("content")
	val content: String,

	@Field("disabled")
	var disabled: Boolean = false,

	@CreatedDate
	@Field("createdAt")
	val createdAt: LocalDateTime? = null,

	@LastModifiedDate
	@Field("modifiedAt")
	val modifiedAt: LocalDateTime? = null
) {
	/**
	 * 메시지 활성화 (디폴트 값)
	 */
	fun activate() {
		disabled = false
	}

	/**
	 * 메시지 삭제
	 */
	fun deactivate() {
		disabled = true
	}
}