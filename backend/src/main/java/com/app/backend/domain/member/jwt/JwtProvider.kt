package com.app.backend.domain.member.jwt

import com.app.backend.domain.member.entity.Member
import com.app.backend.domain.member.entity.MemberDetails
import io.jsonwebtoken.Claims
import io.jsonwebtoken.Jwts
import org.slf4j.LoggerFactory
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.Authentication
import org.springframework.stereotype.Component
import java.time.Instant
import java.util.*
import javax.crypto.SecretKey

private val log = LoggerFactory.getLogger(JwtProvider::class.java)

@Component
class JwtProvider {
    private val key: SecretKey = Jwts.SIG.HS256.key().build()

    companion object {
        private const val ACCESS_TOKEN_EXPIRE_TIME = 1000L * 60 * 30 // 30분
        private const val REFRESH_TOKEN_EXPIRE_TIME = 1000L * 60 * 60 * 24 // 1일
    }

    fun generateAccessToken(member: Member): String {
        return Jwts.builder()
            .claim("name", member.username)
            .claim("id", member.id)
            .claim("role", member.role)
            .claim("password", member.password)
            .claim("provider", member.provider)
            .expiration(Date(System.currentTimeMillis() + ACCESS_TOKEN_EXPIRE_TIME))
            .signWith(key)
            .compact()
    }

    fun generateRefreshToken(): String {
        return Jwts.builder()
            .expiration(Date.from(Instant.now().plusMillis(REFRESH_TOKEN_EXPIRE_TIME)))
            .signWith(key)
            .compact()
    }

    fun validateToken(token: String): Boolean {
        return try {
            Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
            true
        } catch (e: Exception) {
            false
        }
    }

    fun getAuthentication(token: String): Authentication {
        val claims = Jwts.parser()
            .verifyWith(key)
            .build()
            .parseSignedClaims(token)
            .payload

        val member = Member.create(
            claims.get("name", String::class.java),
            claims.get("password", String::class.java),
            claims.get("nickname", String::class.java),
            claims.get("role", String::class.java),
            false,
            null,
            null
        )

        val memberDetails = MemberDetails.of(member)

        return UsernamePasswordAuthenticationToken(
            memberDetails,
            "",
            memberDetails.authorities
        )
    }

    fun getMemberId(token: String): Long {
        val claims = getTokenClaims(token)
        return claims.get("id", Long::class.java)
    }

    fun getRole(token: String): String {
        val claims = getTokenClaims(token)
        return claims.get("role", String::class.java)
    }

    private fun getTokenClaims(token: String): Claims {
        return Jwts.parser()
            .verifyWith(key)
            .build()
            .parseSignedClaims(token)
            .payload
    }
}