package com.app.backend.domain.post.service.post.domain.member;

import com.app.backend.domain.member.util.CommonUtil;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

@TestConfiguration
public class TestConfig {

	@Bean
	@Primary  // 테스트 환경에서 이 구현체 우선 사용
	public CommonUtil commonUtil() {
		return new CommonUtil() {
			@Override
			public void setCookies(Cookie refreshTokenCookie, HttpServletResponse response) {
				// 테스트에서는 단순히 쿠키만 추가. 리다이렉트는 생략
				response.addCookie(refreshTokenCookie);
			}

			@Override
			public void invalidateCookies(HttpServletResponse response) {
				Cookie accessTokenCookie = new Cookie("accessToken", null);
				Cookie refreshTokenCookie = new Cookie("refreshToken", null);
				accessTokenCookie.setMaxAge(0);
				refreshTokenCookie.setMaxAge(0);
				response.addCookie(accessTokenCookie);
				response.addCookie(refreshTokenCookie);
			}
		};
	}
}