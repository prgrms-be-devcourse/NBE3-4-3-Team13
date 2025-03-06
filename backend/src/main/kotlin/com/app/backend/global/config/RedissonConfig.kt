package com.app.backend.global.config

import com.app.backend.global.config.condition.RedisAvailableCondition
import org.redisson.Redisson
import org.redisson.api.RedissonClient
import org.redisson.config.Config
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Conditional
import org.springframework.context.annotation.Configuration

@Configuration
class RedissonConfig {
    @Value("\${redisson.host:localhost}")
    private val host: String? = null

    @Value("\${redisson.port:6380}")
    private val port = 0

    @Value("\${redisson.password}")
    private val password: String? = null

    @Bean
    @Conditional(RedisAvailableCondition::class)
    fun redissonClient(): RedissonClient {
        val config = Config()
        if (!password.isNullOrBlank()) config.useSingleServer()
            .setAddress("redis://%s:%d".formatted(host, port))
            .setPassword(password)
            .setConnectionPoolSize(64)
        else config.useSingleServer()
            .setAddress("redis://%s:%d".formatted(host, port))
            .setConnectionPoolSize(64)
        return Redisson.create(config)
    }
}
