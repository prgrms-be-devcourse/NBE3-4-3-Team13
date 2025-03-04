package com.app.backend.global.rabbitmq;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import com.app.backend.domain.chat.message.dto.response.MessageResponse;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class ChatMessageConsumer {

	private final SimpMessagingTemplate messagingTemplate;

	@RabbitListener(queues = "${rabbitmq.queue.name}")
	public void onMessage(MessageResponse messageResponse) { // Queue에서 message를 구독
		try {
			log.info("Received message: {}", messageResponse);
			messagingTemplate.convertAndSend("/exchange/chat.exchange/chat." + messageResponse.chatRoomId(), messageResponse);
		} catch (Exception e) {
			log.error("Error processing message: " + e.getMessage());
		}
	}
}
