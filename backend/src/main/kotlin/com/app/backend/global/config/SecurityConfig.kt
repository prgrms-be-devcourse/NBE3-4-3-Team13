package com.app.backend.global.config

import com.app.backend.domain.member.jwt.JwtAuthenticationFilter
import com.app.backend.domain.member.jwt.JwtProvider
import com.app.backend.domain.member.oauth.OAuth2SuccessHandler
import com.app.backend.domain.member.service.CustomOAuth2UserService
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.core.AuthenticationException
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.oauth2.core.OAuth2AuthenticationException
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.authentication.AuthenticationFailureHandler
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter
import org.springframework.stereotype.Component
import org.springframework.web.cors.CorsConfiguration
import org.springframework.web.cors.CorsConfigurationSource
import org.springframework.web.cors.UrlBasedCorsConfigurationSource
import org.springframework.web.servlet.config.annotation.EnableWebMvc
import org.springframework.web.util.UriComponentsBuilder

private val log = LoggerFactory.getLogger(SecurityConfig::class.java)

@Configuration
@EnableWebMvc
@EnableWebSecurity
class SecurityConfig(
    private val jwtProvider: JwtProvider,
    private val customOAuth2UserService: CustomOAuth2UserService,
    private val oAuth2SuccessHandler: OAuth2SuccessHandler
) {
    private val origins = arrayOf(
        "http://localhost:3000", // React
        "http://localhost:8080"  // Spring Boot
    )

    @Bean
    fun passwordEncoder() = BCryptPasswordEncoder()

    @Bean
    fun securityFilterChain(http: HttpSecurity): SecurityFilterChain = http
        .csrf { it.disable() }
        .cors { it.configurationSource(corsConfigurationSource()) }
        .sessionManagement { it.sessionCreationPolicy(SessionCreationPolicy.STATELESS) }
        .authorizeHttpRequests { request ->
            request
                .requestMatchers(
                    "/swagger-ui/**",
                    "/swagger-resources/**",
                    "/v3/api-docs/**",
                    "/swagger-ui.html"
                ).permitAll()
                .requestMatchers("/h2-console/**").permitAll()
                .requestMatchers(
                    "/oauth2/authorization/**",
                    "/login/oauth2/code/*"
                ).permitAll()
                .requestMatchers(
                    "/api/v1/members/**",
                    "/images/**",
                    "/ws/**",
                    "/api/v1/proxy/kakao/**",
                    "/api/v1/notifications/**"
                ).permitAll()
                .requestMatchers("/actuator/prometheus").permitAll()
                .anyRequest().authenticated()
        }
        .headers { headers ->
            headers
                .frameOptions { it.sameOrigin() }
                .contentSecurityPolicy { csp ->
                    csp.policyDirectives(
                        "default-src 'self'; script-src 'self' 'unsafe-inline' 'unsafe-eval'; style-src 'self' 'unsafe-inline'; img-src 'self' data: https:; object-src 'none'; base-uri 'self'; connect-src 'self' https://kauth.kakao.com https://kapi.kakao.com; frame-ancestors 'self'; form-action 'self'; block-all-mixed-content"
                    )
                }
        }
        .oauth2Login { oauth2 ->
            oauth2
                .authorizationEndpoint { it.baseUri("/oauth2/authorization/kakao") }
                .redirectionEndpoint { it.baseUri("/login/oauth2/code/*") }
                .userInfoEndpoint { it.userService(customOAuth2UserService) }
                .successHandler(oAuth2SuccessHandler)
                .failureHandler(OAuth2AuthenticationFailureHandler())
        }
        .addFilterBefore(
            JwtAuthenticationFilter(jwtProvider),
            UsernamePasswordAuthenticationFilter::class.java
        )
        .build()

    @Bean
    fun corsConfigurationSource(): CorsConfigurationSource {
        val configuration = CorsConfiguration().apply {
            allowedOrigins = origins.toList()
            allowedMethods = listOf("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS")
            allowedHeaders = listOf(
                "Authorization",
                "Content-Type",
                "Accept",
                "X-Requested-With"
            )
            allowCredentials = true
            maxAge = 3600L
        }

        return UrlBasedCorsConfigurationSource().apply {
            registerCorsConfiguration("/**", configuration)
        }
    }
}

@Component
class OAuth2AuthenticationFailureHandler : AuthenticationFailureHandler {
    override fun onAuthenticationFailure(
        request: HttpServletRequest,
        response: HttpServletResponse,
        exception: AuthenticationException
    ) {
        val errorMessage = getErrorMessage(exception)
        val targetUrl = UriComponentsBuilder
            .fromUriString("http://localhost:3000/login")
            .queryParam("error", errorMessage)
            .build()
            .toUriString()

        response.sendRedirect(targetUrl)
    }

    private fun getErrorMessage(exception: AuthenticationException): String {
        return if (exception is OAuth2AuthenticationException) {
            when (exception.error.errorCode) {
                "invalid_token" -> "유효하지 않은 토큰입니다."
                "invalid_request" -> "잘못된 요청입니다."
                else -> "로그인 처리 중 오류가 발생했습니다: ${exception.error.description}"
            }
        } else {
            "로그인 처리 중 오류가 발생했습니다."
        }
    }
}