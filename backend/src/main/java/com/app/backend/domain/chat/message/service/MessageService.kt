package com.app.backend.domain.chat.message.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import com.app.backend.domain.chat.message.dto.request.MessageRequest;
import com.app.backend.domain.chat.message.dto.response.MessageResponse;
import com.app.backend.domain.chat.message.repository.MessageRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class MessageService {

	private final MessageRepository messageRepository;

	public Page<MessageResponse> getMessagesByChatRoomId(Long chatRoomId, int page, int size) {
		Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Order.desc("createdAt")));
		return messageRepository.findByChatRoomIdAndDisabledFalse(chatRoomId, pageable).map(MessageResponse::from);
	}

	public MessageResponse saveMessage(MessageRequest messageRequest) {
		return MessageResponse.from(messageRepository.save(messageRequest.toEntity()));
	}
}
