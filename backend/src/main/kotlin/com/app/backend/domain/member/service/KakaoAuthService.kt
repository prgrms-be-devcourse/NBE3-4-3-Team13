package com.app.backend.domain.member.service

import com.app.backend.domain.member.dto.kakao.KakaoUserInfo
import com.app.backend.domain.member.dto.kakao.TokenDto
import com.app.backend.domain.member.entity.Member
import com.app.backend.domain.member.exception.MemberErrorCode
import com.app.backend.domain.member.exception.MemberException
import com.app.backend.domain.member.jwt.JwtProvider
import com.app.backend.domain.member.repository.MemberRepository
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.*
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.client.RestTemplate
import org.springframework.web.util.UriComponentsBuilder

private val log = LoggerFactory.getLogger(KakaoAuthService::class.java)

@Service
class KakaoAuthService(
    private val restTemplate: RestTemplate,
    private val jwtProvider: JwtProvider,
    private val memberRepository: MemberRepository,
    @Value("\${spring.security.oauth2.client.registration.kakao.client-id}")
    private val clientId: String,
    @Value("\${spring.security.oauth2.client.registration.kakao.client-secret}")
    private val clientSecret: String,
    @Value("\${spring.security.oauth2.client.registration.kakao.redirect-uri}")
    private val redirectUri: String
) {
    @Transactional
    fun kakaoLogin(code: String): TokenDto {
        return try {
            // 1. 인가코드로 액세스 토큰 요청
            val kakaoAccessToken = getKakaoAccessToken(code)

            // 2. 액세스 토큰으로 카카오 API 호출
            val userInfo = getKakaoUserInfo(kakaoAccessToken)

            // 3. 회원가입 & 로그인 처리
            val member = saveOrUpdate(userInfo)

            // 4. JWT 토큰 발급
            val accessToken = jwtProvider.generateAccessToken(member)
            val refreshToken = jwtProvider.generateRefreshToken()

            memberRepository.save(member)

            TokenDto(userInfo.id, accessToken, refreshToken, "USER")
        } catch (e: Exception) {
            log.error("카카오 로그인 처리 중 오류: ${e.message}")
            if (e.message?.contains("authorization code not found") == true) {
                log.warn("이미 사용된 인증 코드입니다.")
            }
            throw e
        }
    }

    private fun getKakaoAccessToken(code: String): String {
        return try {
            val tokenUri = UriComponentsBuilder
                .fromUriString("https://kauth.kakao.com/oauth/token")
                .queryParam("grant_type", "authorization_code")
                .queryParam("client_id", clientId)
                .queryParam("redirect_uri", redirectUri)
                .queryParam("code", code)
                .queryParam("client_secret", clientSecret)
                .build()
                .toString()

            val headers = HttpHeaders().apply {
                contentType = MediaType.APPLICATION_FORM_URLENCODED
            }

            val request = HttpEntity<Any>(headers)

            val response = restTemplate.exchange<Map<*, *>>(
                tokenUri,
                HttpMethod.GET,
                request,
                Map::class.java
            )

            if (response.statusCode == HttpStatus.OK && response.body != null) {
                return response.body?.get("access_token") as String
            }

            throw MemberException(MemberErrorCode.MEMBER_FAILED_TO_KAKAO_TOKEN)
        } catch (e: Exception) {
            log.error("카카오 토큰 요청 중 오류 발생: ${e.message}")
            throw RuntimeException("카카오 토큰 요청 실패", e)
        }
    }

    fun getKakaoUserInfo(accessToken: String): KakaoUserInfo {
        val headers = HttpHeaders().apply {
            setBearerAuth(accessToken)
        }

        val request = HttpEntity<String>(headers)

        return try {
            val response = restTemplate.exchange<Map<*, *>>(
                "https://kapi.kakao.com/v2/user/me",
                HttpMethod.GET,
                request,
                Map::class.java
            )

            val body = response.body
            val properties = body?.get("properties") as Map<*, *>

            KakaoUserInfo(
                body["id"].toString(),
                properties["nickname"] as String
            )
        } catch (e: Exception) {
            log.error("카카오 API 호출 실패: ${e.message}")
            throw MemberException(MemberErrorCode.MEMBER_FAILED_TO_KAKAO_AUTH)
        }
    }

    private fun saveOrUpdate(userInfo: KakaoUserInfo): Member {
        return memberRepository.findByOauthProviderId(userInfo.id)
            .orElseGet {
                memberRepository.save(
                    Member.create(
                        userInfo.id,
                        "",
                        userInfo.nickname,
                        "ROLE_USER",
                        false,
                        Member.Provider.KAKAO,
                        userInfo.id
                    )
                )
            }
    }
}