package com.app.backend.domain.chat.message.service

import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Service

import com.app.backend.domain.chat.message.dto.request.MessageRequest
import com.app.backend.domain.chat.message.dto.response.MessageResponse
import com.app.backend.domain.chat.message.repository.MessageRepository
import org.springframework.scheduling.annotation.Async

@Service
class MessageService(private val messageRepository: MessageRepository) {

	fun getMessagesByChatRoomId(chatRoomId: Long, page: Int, size: Int): Page<MessageResponse> {
		val pageable: Pageable = PageRequest.of(page, size, Sort.by(Sort.Order.desc("createdAt")))
		return messageRepository.findByChatRoomIdAndDisabledFalse(chatRoomId, pageable).map { MessageResponse.from(it) }
	}

	@Async
	fun saveMessageAsync(messageRequest: MessageRequest) {
		MessageResponse.from(messageRepository.save(messageRequest.toEntity()))
	}
}
