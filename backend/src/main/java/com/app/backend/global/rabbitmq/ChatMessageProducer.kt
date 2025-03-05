package com.app.backend.global.rabbitmq;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.app.backend.domain.chat.message.dto.response.MessageResponse;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class ChatMessageProducer {

	private final RabbitTemplate rabbitTemplate;

	@Value("${rabbitmq.exchange.name}")
	private String exchange;

	public void sendMessage(MessageResponse message, String roomId) {
		log.info("message send : {}", message);
		rabbitTemplate.convertAndSend(exchange, roomId, message);
	}
}
