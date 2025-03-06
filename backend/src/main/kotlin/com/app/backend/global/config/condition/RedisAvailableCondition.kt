package com.app.backend.global.config.condition

import org.springframework.context.annotation.Condition
import org.springframework.context.annotation.ConditionContext
import org.springframework.core.type.AnnotatedTypeMetadata
import redis.clients.jedis.Jedis

class RedisAvailableCondition : Condition {
    override fun matches(context: ConditionContext, metadata: AnnotatedTypeMetadata): Boolean {
        val host = context.environment.getProperty("redisson.host", "localhost")
        val port = context.environment.getProperty("redisson.port", "6380").toInt()
        val password = context.environment.getProperty("redisson.password", "")

        try {
            Jedis(host, port).use { jedis ->
                if (password.isNotEmpty()) jedis.auth(password)
                return "PONG" == jedis.ping()
            }
        } catch (e: Exception) {
            return false
        }
    }
}
