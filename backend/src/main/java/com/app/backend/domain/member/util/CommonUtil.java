package com.app.backend.domain.member.util;

import org.springframework.stereotype.Component;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class CommonUtil {

	public void setCookies(Cookie refreshTokenCookie, HttpServletResponse response) {
		refreshTokenCookie.setHttpOnly(true);
		refreshTokenCookie.setSecure(true);
		refreshTokenCookie.setPath("/");
		refreshTokenCookie.setMaxAge(1800); // 30분

		String cookieHeader = String.format("%s=%s; Path=%s; Max-Age=%d; HttpOnly; Secure; SameSite=Strict",
				refreshTokenCookie.getName(),
				refreshTokenCookie.getValue() != null ? refreshTokenCookie.getValue() : "",
				refreshTokenCookie.getPath(),
				refreshTokenCookie.getMaxAge());

		response.setHeader("Set-Cookie", cookieHeader);
		response.addCookie(refreshTokenCookie);
	}

	public void invalidateCookies(HttpServletResponse response) {
		// 기존 쿠키 무효화
		Cookie refreshTokenCookie = new Cookie("refreshToken", null);

		// 쿠키 설정
		refreshTokenCookie.setHttpOnly(true);
		refreshTokenCookie.setSecure(true);
		refreshTokenCookie.setPath("/");
		refreshTokenCookie.setMaxAge(0); // 즉시 만료

		// 올바른 쿠키 형식으로 헤더 설정
		String cookieHeader = String.format("%s=%s; Path=%s; Max-Age=%d; HttpOnly; Secure; SameSite=Strict",
				refreshTokenCookie.getName(),
				refreshTokenCookie.getValue() != null ? refreshTokenCookie.getValue() : "",
				refreshTokenCookie.getPath(),
				refreshTokenCookie.getMaxAge());

		response.addHeader("Set-Cookie", cookieHeader);
		response.addCookie(refreshTokenCookie);
	}
}
