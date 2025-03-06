package com.app.backend.global.config

import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class AppConfig {
    @Bean
    fun addModules() = Jackson2ObjectMapperBuilderCustomizer {
        it.modules(JavaTimeModule())
        it.featuresToDisable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
    }
}
