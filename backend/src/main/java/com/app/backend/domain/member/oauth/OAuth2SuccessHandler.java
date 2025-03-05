package com.app.backend.domain.member.oauth;

import com.app.backend.domain.member.entity.Member;
import com.app.backend.domain.member.exception.MemberErrorCode;
import com.app.backend.domain.member.jwt.JwtProvider;
import com.app.backend.domain.member.repository.MemberRepository;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;

@Slf4j
@Component
@RequiredArgsConstructor
public class OAuth2SuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final JwtProvider jwtProvider;
    private final MemberRepository memberRepository;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
        Authentication authentication) throws IOException, ServletException {
        
        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();
        String oauthId = String.valueOf(oAuth2User.getAttributes().get("id"));

        Member member = memberRepository.findByOauthProviderId(oauthId)
            .orElseThrow(() -> new MemberException(MemberErrorCode.MEMBER_NOT_FOUND));

        // 토큰 생성
        String accessToken = jwtProvider.generateAccessToken(member);
        String refreshToken = jwtProvider.generateRefreshToken();

        // refreshToken 쿠키 설정
        Cookie refreshTokenCookie = new Cookie("refreshToken", refreshToken);
        refreshTokenCookie.setHttpOnly(true);
        refreshTokenCookie.setSecure(true);
        refreshTokenCookie.setPath("/");
        refreshTokenCookie.setMaxAge(1800); // 30분

        String cookieHeader = String.format("%s; SameSite=Strict", refreshTokenCookie.toString());
        response.setHeader("Set-Cookie", cookieHeader);
        response.addCookie(refreshTokenCookie);

        // 프론트엔드로 리다이렉트 (회원 정보 포함)
        String targetUrl = UriComponentsBuilder.fromUriString("http://localhost:3000")
            .queryParam("accessToken", accessToken)
            .queryParam("nickname", member.getNickname())
            .queryParam("role", member.getRole())
            .build().toUriString();

        getRedirectStrategy().sendRedirect(request, response, targetUrl);
    }
} 