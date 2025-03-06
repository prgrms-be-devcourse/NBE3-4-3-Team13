package com.app.backend.domain.member.util

import jakarta.servlet.http.Cookie
import jakarta.servlet.http.HttpServletResponse
import org.springframework.stereotype.Component

@Component
class CommonUtil {
    fun setCookies(refreshTokenCookie: Cookie, response: HttpServletResponse) {
        refreshTokenCookie.apply {
            isHttpOnly = true
            secure = true
            path = "/"
            maxAge = 1800 // 30분
        }

        val cookieHeader = buildCookieHeader(refreshTokenCookie)

        response.setHeader("Set-Cookie", cookieHeader)
        response.addCookie(refreshTokenCookie)
    }

    fun invalidateCookies(response: HttpServletResponse) {
        // 기존 쿠키 무효화
        val refreshTokenCookie = Cookie("refreshToken", null).apply {
            isHttpOnly = true
            secure = true
            path = "/"
            maxAge = 0 // 즉시 만료
        }

        val cookieHeader = buildCookieHeader(refreshTokenCookie)

        response.addHeader("Set-Cookie", cookieHeader)
        response.addCookie(refreshTokenCookie)
    }

    private fun buildCookieHeader(cookie: Cookie): String {
        return "%s=%s; Path=%s; Max-Age=%d; HttpOnly; Secure; SameSite=Strict".format(
            cookie.name,
            cookie.value ?: "",
            cookie.path,
            cookie.maxAge
        )
    }
}