package com.app.backend.global.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

	@Override
	public void configureMessageBroker(MessageBrokerRegistry registry) {
		// 기본 메시지 브로커를 RabbitMQ로 설정
		registry.setApplicationDestinationPrefixes("/pub")
			.setPathMatcher(new AntPathMatcher("."))
			.enableStompBrokerRelay("/exchange", "/topic", "/queue")
			// .setRelayHost("linkus-rabbitmq")
			.setRelayHost("localhost")
			.setRelayPort(61613)
			.setClientLogin("guest")
			.setClientPasscode("guest")
			.setVirtualHost("/");
	}

	@Override
	public void registerStompEndpoints(StompEndpointRegistry registry) {
		// 클라이언트가 연결할 엔드포인트 설정
		registry.addEndpoint("/ws/chat")
			.setAllowedOriginPatterns("*")
			.withSockJS();

		// notification 엔드포인트 추가
		registry.addEndpoint("/ws-notification")
				.setAllowedOrigins("*")
				.withSockJS();
	}
}
