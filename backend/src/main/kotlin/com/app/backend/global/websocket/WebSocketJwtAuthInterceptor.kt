package com.app.backend.global.websocket

import com.app.backend.domain.member.jwt.JwtProvider
import com.app.backend.global.websocket.service.WebSocketSessionService
import io.github.oshai.kotlinlogging.KLogger
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.messaging.Message
import org.springframework.messaging.MessageChannel
import org.springframework.messaging.simp.stomp.StompCommand
import org.springframework.messaging.simp.stomp.StompHeaderAccessor
import org.springframework.messaging.support.ChannelInterceptor
import org.springframework.messaging.support.MessageHeaderAccessor
import org.springframework.stereotype.Component

@Component
class WebSocketSessionInterceptor(
    private val webSocketSessionService: WebSocketSessionService,
    private val jwtProvider: JwtProvider
) : ChannelInterceptor {

    private val log: KLogger = KotlinLogging.logger { }

    override fun preSend(
        message: Message<*>,
        channel: MessageChannel
    ): Message<*>? {
        val accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor::class.java)

        accessor?.let {
            when (accessor.command) {
                StompCommand.CONNECT -> {
                    // 연결 시 토큰에서 memberId 추출
                    val sessionId = accessor.sessionId
                    val token = accessor.getNativeHeader("Authorization")?.firstOrNull()
                        ?.replace("Bearer ", "")
                    log.info { "sessionId: $sessionId, token: $token" }

                    if (sessionId != null && token != null) {
                        // 토큰을 이용하여 memberId 추출, 핸드 쉐이크 과정에서 검증을 했으므로 검증 생략함.
                        val memberId = jwtProvider.getMemberId(token)
                        webSocketSessionService.saveSession(sessionId, memberId.toString())
                    }
                }
                StompCommand.DISCONNECT -> {
                    // 연결 해제 시 세션 제거
                    val sessionId = accessor.sessionId
                    if (sessionId != null) {
                        webSocketSessionService.removeSession(sessionId)
                    }
                }
                else -> { /* 다른 명령어는 처리하지 않음 */ }
            }
        }

        return message
    }
}
