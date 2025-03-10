package com.app.backend.domain.notification

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter
import java.io.IOException
import java.util.concurrent.ConcurrentHashMap

@Component
class SseEmitters {
    private val emitters = ConcurrentHashMap<String, SseEmitter>()
    private val log = LoggerFactory.getLogger(SseEmitters::class.java)

    fun add(userId: String, emitter: SseEmitter) {
        // 기존 emitter가 있다면 완전히 정리
        emitters[userId]?.let { existingEmitter ->
            try {
                existingEmitter.complete()
            } catch (e: Exception) {
                log.warn("Failed to complete existing emitter for user: {}", userId)
            }
        }
        
        emitters[userId] = emitter
        log.info(
            "New SSE emitter added for user: {}. Total active emitters: {}",
            userId,
            emitters.size
        )
    }

    fun remove(userId: String): SseEmitter? {
        val removed = emitters.remove(userId)
        try {
            removed?.complete()
        } catch (e: Exception) {
            log.warn("Failed to complete emitter while removing for user: {}", userId)
        }
        log.info("Removed SSE emitter for user: {}. Remaining emitters: {}", userId, emitters.size)
        return removed
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
                remove(userId)  // remove() 메서드 사용
            }
        } ?: run {
            log.debug("No SSE emitter found for user: {}", userId)
        }
    }

    // 현재 활성 연결 수 확인용 메서드 추가
    fun getActiveEmittersCount(): Int {
        return emitters.size
    }
}