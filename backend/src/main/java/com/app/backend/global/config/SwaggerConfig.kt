package com.app.backend.global.config

import io.swagger.v3.oas.models.Components
import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.info.Info
import io.swagger.v3.oas.models.security.SecurityRequirement
import io.swagger.v3.oas.models.security.SecurityScheme
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class SwaggerConfig {
    @Bean
    fun openAPI(): OpenAPI {
        val jwt = "JWT"
        val securityRequirement = SecurityRequirement().addList(jwt)
        val components = Components()
            .addSecuritySchemes(jwt, SecurityScheme()
                .name(jwt)
                .type(SecurityScheme.Type.HTTP)
                .scheme("bearer")
                .bearerFormat("JWT")
            )

        return OpenAPI()
            .info(
                Info()
                .title("API 문서")
                .description("API 명세서")
                .version("1.0.0"))
            .addSecurityItem(securityRequirement)
            .components(components)
    }
}