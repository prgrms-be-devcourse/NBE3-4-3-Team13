package com.app.backend.domain.chat.room.exception

import com.app.backend.global.error.exception.DomainErrorCode
import org.springframework.http.HttpStatus

enum class ChatRoomErrorCode(
	private val status: HttpStatus,
	private val code: String,
	private val message: String
) : DomainErrorCode {
	CHAT_ROOM_NOT_FOUND(HttpStatus.NOT_FOUND, "CH001", "채팅방을 찾지 못했습니다.");

	override fun getStatus(): HttpStatus = status
	override fun getCode(): String = code
	override fun getMessage(): String = message
}
