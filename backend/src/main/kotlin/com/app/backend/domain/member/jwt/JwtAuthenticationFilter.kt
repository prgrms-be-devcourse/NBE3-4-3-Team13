package com.app.backend.domain.member.jwt

import jakarta.servlet.FilterChain
import jakarta.servlet.ServletException
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.filter.OncePerRequestFilter
import java.io.IOException

class JwtAuthenticationFilter(
    private val jwtProvider: JwtProvider
) : OncePerRequestFilter() {

    @Throws(ServletException::class, IOException::class)
    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        try {
            extractToken(request.getHeader("Authorization"))?.let { token ->
                if (jwtProvider.validateToken(token)) {
                    val authentication = jwtProvider.getAuthentication(token)
                    SecurityContextHolder.getContext().authentication = authentication
                }
            }
        } catch (e: Exception) {
            SecurityContextHolder.clearContext()
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid token")
            return
        }

        filterChain.doFilter(request, response)
    }

    private fun extractToken(bearerToken: String?): String? {
        return bearerToken?.takeIf { it.startsWith("Bearer ") }?.substring(7)
    }
}