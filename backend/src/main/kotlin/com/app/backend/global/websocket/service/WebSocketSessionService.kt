package com.app.backend.global.websocket.service

import org.springframework.data.redis.core.RedisTemplate
import org.springframework.stereotype.Service

@Service
class WebSocketSessionService(private val sessionRedisTemplate: RedisTemplate<String, String>) {

    private val SESSION_KEY = "websocket-sessions"

    fun saveSession(sessionId: String, memberId: String) {
        sessionRedisTemplate.opsForHash<String, String>().put(SESSION_KEY, sessionId, memberId)
    }

    // 세션을 Redis에서 삭제
    fun removeSession(sessionId: String) {
        sessionRedisTemplate.opsForHash<String, String>().delete(SESSION_KEY, sessionId)
    }
}