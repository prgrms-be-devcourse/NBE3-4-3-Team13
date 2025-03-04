package com.app.backend.domain.member.jwt;

import java.time.Instant;
import java.util.Date;

import javax.crypto.SecretKey;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import com.app.backend.domain.member.entity.Member;
import com.app.backend.domain.member.entity.MemberDetails;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtProvider {
	private final SecretKey key = Jwts.SIG.HS256.key().build();  // 키를 상수로 저장
	
	private final long ACCESS_TOKEN_EXPIRE_TIME = 1000 * 60 * 30; // 30분
	private final long REFRESH_TOKEN_EXPIRE_TIME = 1000 * 60 * 60 * 24; // 1일

	// access token 생성
	public String generateAccessToken(Member member) {
		return Jwts.builder()
			.claim("name", member.getUsername())
			.claim("id", member.getId())
			.claim("role", member.getRole())
			.claim("password", member.getPassword())
			.claim("provider", member.getProvider())
			.expiration(new Date(System.currentTimeMillis() + ACCESS_TOKEN_EXPIRE_TIME))
			.signWith(key)  // 저장된 키 사용
			.compact();
	}

	// refresh token 생성
	public String generateRefreshToken() {
		return Jwts.builder()
			.expiration(Date.from(Instant.now().plusMillis(REFRESH_TOKEN_EXPIRE_TIME)))
			.signWith(key)  // 저장된 키 사용
			.compact();
	}

	// 토큰 유효성 검증
	public boolean validateToken(String token) {
		try {
			Jwts.parser()
				.verifyWith(key)  // 저장된 키 사용
				.build()
				.parseSignedClaims(token);
			return true;
		} catch (Exception e) {
			return false;
		}
	}

	public Authentication getAuthentication(String token) {
		// 토큰에서 모든 필요한 정보를 추출
        Claims claims = Jwts.parser()
            .verifyWith(key)  // 저장된 키 사용
            .build()
            .parseSignedClaims(token)
            .getPayload();

        // 토큰의 claim에서 필요한 정보를 가져옴
		Member member = Member.builder()
			.id(claims.get("id", Long.class))
			.username(claims.get("name", String.class))
			.role(claims.get("role", String.class))
			.password(claims.get("password", String.class))
			.nickname(claims.get("nickname", String.class))
			.build();
		MemberDetails memberDetails = MemberDetails.of(member);

		// Authentication 객체 생성 및 반환
		return new UsernamePasswordAuthenticationToken(
			memberDetails,
			"",
			memberDetails.getAuthorities()
		);
	}
	// 토큰을 통해 id 추출
	public Long getMemberId(String token) {
		Claims claims = Jwts.parser()
			.verifyWith(key)  // 저장된 키 사용
			.build()
			.parseSignedClaims(token)
			.getPayload();
		return claims.get("id", Long.class);
	}

	// 토큰을 통해 role 추출
	public String getRole(String token) {
		Claims claims = Jwts.parser()
			.verifyWith(key)  // 저장된 키 사용
			.build()
			.parseSignedClaims(token)
			.getPayload();
		return claims.get("role", String.class);
	}

}
