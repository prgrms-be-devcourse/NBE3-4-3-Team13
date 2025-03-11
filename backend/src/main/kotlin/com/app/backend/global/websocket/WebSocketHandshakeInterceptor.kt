package com.app.backend.global.websocket

import com.app.backend.domain.member.jwt.JwtProvider
import io.github.oshai.kotlinlogging.KLogger
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.http.server.ServerHttpRequest
import org.springframework.http.server.ServerHttpResponse
import org.springframework.stereotype.Component
import org.springframework.web.socket.WebSocketHandler
import org.springframework.web.socket.server.HandshakeInterceptor
import org.springframework.web.util.UriComponentsBuilder

@Component
class WebSocketHandshakeInterceptor(
    private val jwtProvider: JwtProvider
) : HandshakeInterceptor {

    private val log: KLogger = KotlinLogging.logger {}

    override fun beforeHandshake(
        request: ServerHttpRequest,
        response: ServerHttpResponse,
        wsHandler: WebSocketHandler,
        attributes: MutableMap<String, Any>
    ): Boolean {
        val uri = request.uri
        val queryParams = UriComponentsBuilder.fromUri(uri).build().queryParams

        val token = queryParams["token"]?.firstOrNull()
        if (token.isNullOrBlank()) {
            log.warn { "WebSocket connection rejected: Missing token" }
            return false
        }

        return try {
            val memberId = jwtProvider.getMemberId(token)
//            attributes["memberId"] = memberId
            log.info { "WebSocket connection accepted for memberId: $memberId" }
            true
        } catch (e: Exception) {
            log.warn { "WebSocket connection rejected: Invalid token" }
            false
        }
    }

    override fun afterHandshake(
        request: ServerHttpRequest,
        response: ServerHttpResponse,
        wsHandler: WebSocketHandler,
        exception: Exception?
    ) {
    }
}