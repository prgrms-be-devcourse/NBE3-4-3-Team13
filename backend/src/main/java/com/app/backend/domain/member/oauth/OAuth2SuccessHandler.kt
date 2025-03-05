package com.app.backend.domain.member.oauth

import com.app.backend.domain.member.exception.MemberErrorCode
import com.app.backend.domain.member.exception.MemberException
import com.app.backend.domain.member.jwt.JwtProvider
import com.app.backend.domain.member.repository.MemberRepository
import jakarta.servlet.ServletException
import jakarta.servlet.http.Cookie
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.slf4j.LoggerFactory
import org.springframework.security.core.Authentication
import org.springframework.security.oauth2.core.user.OAuth2User
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler
import org.springframework.stereotype.Component
import org.springframework.web.util.UriComponentsBuilder
import java.io.IOException

private val log = LoggerFactory.getLogger(OAuth2SuccessHandler::class.java)

@Component
class OAuth2SuccessHandler(
    private val jwtProvider: JwtProvider,
    private val memberRepository: MemberRepository
) : SimpleUrlAuthenticationSuccessHandler() {

    @Throws(IOException::class, ServletException::class)
    override fun onAuthenticationSuccess(
        request: HttpServletRequest,
        response: HttpServletResponse,
        authentication: Authentication
    ) {
        val oAuth2User = authentication.principal as OAuth2User
        val oauthId = oAuth2User.attributes["id"].toString()

        val member = memberRepository.findByOauthProviderId(oauthId)
            .orElseThrow { MemberException(MemberErrorCode.MEMBER_NOT_FOUND) }

        // 토큰 생성
        val accessToken = jwtProvider.generateAccessToken(member)
        val refreshToken = jwtProvider.generateRefreshToken()

        // refreshToken 쿠키 설정
        val refreshTokenCookie = Cookie("refreshToken", refreshToken).apply {
            isHttpOnly = true
            secure = true
            path = "/"
            maxAge = 1800 // 30분
        }

        val cookieHeader = "${refreshTokenCookie}; SameSite=Strict"
        response.setHeader("Set-Cookie", cookieHeader)
        response.addCookie(refreshTokenCookie)

        // 프론트엔드로 리다이렉트 (회원 정보 포함)
        val targetUrl = UriComponentsBuilder.fromUriString("http://localhost:3000")
            .queryParam("accessToken", accessToken)
            .queryParam("nickname", member.nickname)
            .queryParam("role", member.role)
            .build()
            .toUriString()

        redirectStrategy.sendRedirect(request, response, targetUrl)
    }
}