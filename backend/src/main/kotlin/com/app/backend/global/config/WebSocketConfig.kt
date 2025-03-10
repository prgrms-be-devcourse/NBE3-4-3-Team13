package com.app.backend.global.config

import com.app.backend.global.websocket.WebSocketHandshakeInterceptor
import com.app.backend.global.websocket.WebSocketSessionInterceptor
import org.springframework.context.annotation.Configuration
import org.springframework.messaging.simp.config.ChannelRegistration
import org.springframework.messaging.simp.config.MessageBrokerRegistry
import org.springframework.util.AntPathMatcher
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker
import org.springframework.web.socket.config.annotation.StompEndpointRegistry
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer

@Configuration
@EnableWebSocketMessageBroker
class WebSocketConfig(
    private val webSocketHandshakeInterceptor: WebSocketHandshakeInterceptor,
    private val webSocketSessionInterceptor: WebSocketSessionInterceptor
) : WebSocketMessageBrokerConfigurer {

    override fun configureMessageBroker(registry: MessageBrokerRegistry) {
        registry.setApplicationDestinationPrefixes("/pub")
            .setPathMatcher(AntPathMatcher("."))
            .enableStompBrokerRelay("/exchange", "/topic", "/queue")
            .setRelayHost("localhost")
            .setRelayPort(61613)
            .setClientLogin("guest")
            .setClientPasscode("guest")
            .setVirtualHost("/")
    }

    override fun registerStompEndpoints(registry: StompEndpointRegistry) {
        registry.addEndpoint("/ws/chat")
            .addInterceptors(webSocketHandshakeInterceptor)
            .setAllowedOriginPatterns("*")
            .withSockJS()

        registry.addEndpoint("/ws-notification")
            .setAllowedOrigins("*")
            .withSockJS()
    }

    override fun configureClientInboundChannel(registration: ChannelRegistration) {
        registration.interceptors(webSocketSessionInterceptor)
    }
}
