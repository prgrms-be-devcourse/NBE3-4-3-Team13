package com.app.backend.domain.chat.message.controller;

import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Controller;

import com.app.backend.domain.chat.message.dto.request.MessageRequest;
import com.app.backend.domain.chat.message.dto.response.MessageResponse;
import com.app.backend.domain.chat.message.service.MessageService;
import com.app.backend.global.rabbitmq.ChatMessageProducer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Controller
@RequiredArgsConstructor
@Slf4j
public class MessageWebSocketController {

	private final MessageService messageService;
	private final ChatMessageProducer chatMessageProducer;

	@MessageMapping("chat.{chatRoomId}")
	public void sendMessage(@Payload MessageRequest messageRequest) {
		// 메세지 저장
		MessageResponse messageResponse = messageService.saveMessage(messageRequest);
		chatMessageProducer.sendMessage(messageResponse, "chatroom." + messageRequest.chatRoomId());
	}
}
