package com.app.backend.global.config

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonTypeInfo
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.jsontype.BasicPolymorphicTypeValidator
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.fasterxml.jackson.module.paramnames.ParameterNamesModule
import org.redisson.Redisson
import org.redisson.api.RedissonClient
import org.redisson.config.Config
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.redis.connection.RedisPassword
import org.springframework.data.redis.connection.RedisStandaloneConfiguration
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.data.redis.repository.configuration.EnableRedisRepositories
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer
import org.springframework.data.redis.serializer.StringRedisSerializer

@Configuration
@EnableRedisRepositories
class RedisConfig(
    @Value("\${spring.data.redis.host:localhost}") private val host: String,
    @Value("\${spring.data.redis.port:6379}") private val port: Int,
    @Value("\${spring.data.redis.password}") private val passwordStr: String
) {
    @Bean
    fun redisConnectionFactory() =
        LettuceConnectionFactory(RedisStandaloneConfiguration(host, port).apply {
            this.password = RedisPassword.of(passwordStr)
        })

    @Bean
    fun redisTemplate() =
        RedisTemplate<String, Any>().apply {
            connectionFactory = redisConnectionFactory()
            keySerializer = StringRedisSerializer()
            valueSerializer = GenericJackson2JsonRedisSerializer(ObjectMapper().apply {
                registerModule(KotlinModule.Builder().build())
                registerModule(ParameterNamesModule(JsonCreator.Mode.PROPERTIES))
                registerModule(Jdk8Module())
                registerModule(JavaTimeModule())
                activateDefaultTyping(
                    BasicPolymorphicTypeValidator.builder().allowIfBaseType(Any::class.java).build(),
                    ObjectMapper.DefaultTyping.EVERYTHING,
                    JsonTypeInfo.As.PROPERTY
                )
            })
        }

    @Bean
    fun sessionRedisTemplate(redisConnectionFactory: RedisConnectionFactory): RedisTemplate<String, String> {
        return RedisTemplate<String, String>().apply {
            connectionFactory = redisConnectionFactory
            keySerializer = StringRedisSerializer()
            hashKeySerializer = StringRedisSerializer() // 해시 키를 문자열로 저장
            hashValueSerializer = StringRedisSerializer() // 해시 값을 문자열로 저장
        }
    }

    @Bean
    fun redissonClient(): RedissonClient {
        val config = Config()
        if (passwordStr.isNotBlank()) config.useSingleServer()
            .setAddress("redis://$host:$port")
            .setPassword(passwordStr)
            .setConnectionPoolSize(64)
        else config.useSingleServer()
            .setAddress("redis://$host:$port")
            .setConnectionPoolSize(64)
        return Redisson.create(config)
    }
}