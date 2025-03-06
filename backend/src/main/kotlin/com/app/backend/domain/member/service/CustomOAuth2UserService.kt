package com.app.backend.domain.member.service

import com.app.backend.domain.member.entity.Member
import com.app.backend.domain.member.repository.MemberRepository
import org.slf4j.LoggerFactory
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService
import org.springframework.security.oauth2.core.user.DefaultOAuth2User
import org.springframework.security.oauth2.core.user.OAuth2User
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

private val log = LoggerFactory.getLogger(CustomOAuth2UserService::class.java)

@Service
class CustomOAuth2UserService(
    private val memberRepository: MemberRepository
) : OAuth2UserService<OAuth2UserRequest, OAuth2User> {

    @Transactional
    override fun loadUser(userRequest: OAuth2UserRequest): OAuth2User {
        val delegate: OAuth2UserService<OAuth2UserRequest, OAuth2User> = DefaultOAuth2UserService()
        val oAuth2User = delegate.loadUser(userRequest)

        val userNameAttributeName = userRequest.clientRegistration
            .providerDetails.userInfoEndpoint.userNameAttributeName

        val attributes = oAuth2User.attributes

        // 카카오 계정 정보 추출
        val kakaoAccount = attributes["kakao_account"] as Map<*, *>
        val profile = kakaoAccount["profile"] as Map<*, *>

        val nickname = profile["nickname"] as String
        val oauthId = attributes["id"].toString()

        val member = memberRepository.findByOauthProviderId(oauthId)
            .orElseGet { createMember(oauthId, nickname) }

        return DefaultOAuth2User(
            setOf(SimpleGrantedAuthority(member.role)),
            attributes,
            userNameAttributeName
        )
    }

    private fun createMember(oauthId: String, nickname: String): Member {
        val member = Member.create(
            oauthId,
            "",
            nickname,
            "ROLE_USER",
            false,
            Member.Provider.KAKAO,
            oauthId
        )

        return memberRepository.save(member)
    }
}