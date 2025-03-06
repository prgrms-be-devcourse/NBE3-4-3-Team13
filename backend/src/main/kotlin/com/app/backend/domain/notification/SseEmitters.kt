package com.app.backend.domain.notification

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter
import java.io.IOException
import java.util.concurrent.ConcurrentHashMap

private val log = LoggerFactory.getLogger(SseEmitters::class.java)

@Component
class SseEmitters {
    private val emitters = ConcurrentHashMap<String, SseEmitter>()

    fun add(userId: String, emitter: SseEmitter) {
        emitters[userId] = emitter
        log.debug(
            "New SSE emitter added for user: {}. Total active emitters: {}",
            userId,
            emitters.size
        )
    }

    fun remove(userId: String) {
        emitters.remove(userId)
    }

    fun sendToUser(userId: String, data: Any) {
        emitters[userId]?.let { emitter ->
            try {
                emitter.send(
                    SseEmitter.event()
                        .name("notification")
                        .data(data)
                )
                log.debug("SSE sent successfully to user: {}", userId)
            } catch (e: IOException) {
                log.error("SSE 전송 실패 for user {}: {}", userId, e.message)
                emitters.remove(userId)
            }
        } ?: run {
            log.debug("No SSE emitter found for user: {}", userId)
        }
    }
}