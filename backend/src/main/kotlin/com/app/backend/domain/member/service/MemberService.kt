package com.app.backend.domain.member.service

import com.app.backend.domain.group.dto.response.GroupMembershipResponse
import com.app.backend.domain.group.repository.GroupMembershipRepository
import com.app.backend.domain.member.dto.request.MemberLoginRequestDto
import com.app.backend.domain.member.dto.request.MemberModifyRequestDto
import com.app.backend.domain.member.dto.response.MemberJoinResponseDto
import com.app.backend.domain.member.dto.response.MemberLoginResponseDto
import com.app.backend.domain.member.dto.response.MemberModifyResponseDto
import com.app.backend.domain.member.entity.Member
import com.app.backend.domain.member.exception.MemberErrorCode
import com.app.backend.domain.member.exception.MemberException
import com.app.backend.domain.member.jwt.JwtProvider
import com.app.backend.domain.member.repository.MemberRepository
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime
import java.util.*

private val log = LoggerFactory.getLogger(MemberService::class.java)

@Service
@Transactional(readOnly = true)
class MemberService(
    private val memberRepository: MemberRepository,
    private val groupMembershipRepository: GroupMembershipRepository,
    private val passwordEncoder: PasswordEncoder,
    private val jwtProvider: JwtProvider
) {
    private val disabled = false

    @Transactional
    fun createMember(username: String, password: String, nickname: String): MemberJoinResponseDto {
        memberRepository.findByUsernameAndDisabled(username, disabled)
            .ifPresent { throw MemberException(MemberErrorCode.MEMBER_USERNAME_EXISTS) }

        memberRepository.findByNicknameAndDisabled(nickname, disabled)
            .ifPresent { throw MemberException(MemberErrorCode.MEMBER_NICKNAME_EXISTS) }

        val member = Member.create(
            username,
            passwordEncoder.encode(password),
            nickname,
            "ROLE_ADMIN",
            false,
            null,
            null
        )

        val savedMember = memberRepository.save(member)
        return MemberJoinResponseDto.from(savedMember)
    }

    fun login(request: MemberLoginRequestDto): MemberLoginResponseDto {
        val member = memberRepository.findByUsernameAndDisabled(request.username, disabled)
            .orElseThrow { MemberException(MemberErrorCode.MEMBER_NOT_FOUND) }

        if (!passwordEncoder.matches(request.password, member.password)) {
            throw MemberException(MemberErrorCode.MEMBER_PASSWORD_NOT_MATCH)
        }

        val accessToken = jwtProvider.generateAccessToken(member)
        val refreshToken = jwtProvider.generateRefreshToken()

        memberRepository.save(member)

        return MemberLoginResponseDto.of(member, accessToken, refreshToken)
    }

    @Transactional
    fun logout(token: String) {
        try {
            val member = getCurrentMember(token)
            memberRepository.save(member)
        } catch (e: Exception) {
            throw MemberException(MemberErrorCode.MEMBER_FAILED_LOGOUT)
        }
    }

    fun getCurrentMember(accessToken: String): Member {
        return Optional.ofNullable(accessToken)
            .map { if (it.startsWith("Bearer ")) it.substring(7) else it }
            .filter { jwtProvider.validateToken(it) }
            .map { validateToken ->
                val memberId: Long = jwtProvider.getMemberId(validateToken)
                memberRepository.findByIdAndDisabled(memberId, false)
                    .orElseThrow { MemberException(MemberErrorCode.MEMBER_NOT_FOUND) }
            }
            .orElseThrow { MemberException(MemberErrorCode.MEMBER_UNVALID_TOKEN) }
    }

    @Transactional
    fun modifyMember(member: Member, request: MemberModifyRequestDto): MemberModifyResponseDto {
        val existingMember = memberRepository.findByIdAndDisabled(member.id, false)
            .orElseThrow { MemberException(MemberErrorCode.MEMBER_NOT_FOUND) }

        val updatedMember = existingMember.update(
            password = request.password?.let { passwordEncoder.encode(it) },
            nickname = request.nickname
        )

        val savedMember = memberRepository.save(updatedMember)
        return MemberModifyResponseDto.of(savedMember)
    }

    fun findAllMembers(token: String): Optional<List<Member>> {
        return Optional.ofNullable(token)
            .map { if (it.startsWith("Bearer ")) it.substring(7) else it }
            .filter { jwtProvider.validateToken(it) }
            .filter { validateToken ->
                val role = jwtProvider.getRole(validateToken)
                if (!role.contains("ADMIN")) {
                    throw MemberException(MemberErrorCode.MEMBER_NO_ADMIN_PERMISSION)
                }
                true
            }
            .map { memberRepository.findAllByOrderByIdDesc() }
    }

    @Transactional
    fun deleteMember(token: String) {
        val member = getCurrentMember(token)
        val existingMember = memberRepository.findByIdAndDisabled(member.id, false)
            .orElseThrow { MemberException(MemberErrorCode.MEMBER_NOT_FOUND) }
        
        val deletedMember = existingMember.softDelete()
        memberRepository.save(deletedMember)
    }

    @Transactional
    @Scheduled(fixedRate = 60000 * 30) // 30분마다 실행
    fun cleanupDisabledMembers() {
        log.info("비활성화된 회원 정보 삭제 작업 시작")
        val cutoffDate = LocalDateTime.now().minusSeconds(30)
        val deletedCount = memberRepository.deleteByDisabledIsTrueAndModifiedAtLessThan(cutoffDate)
        log.info("삭제된 회원 수: {}", deletedCount)
    }

    fun getMyGroup(token: String): List<GroupMembershipResponse.Detail> {
        return Optional.ofNullable(token)
            .map { if (it.startsWith("Bearer ")) it.substring(7) else it }
            .filter { jwtProvider.validateToken(it) }
            .map { validateToken ->
                val id = jwtProvider.getMemberId(validateToken)
                groupMembershipRepository.findAllByMemberIdAndDisabled(id, false)
                    .stream()
                    .map { GroupMembershipResponse.toDetail(it) }
                    .toList()
            }
            .orElse(emptyList())
    }
}