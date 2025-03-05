package com.app.backend.domain.member.service;

import com.app.backend.domain.group.dto.response.GroupMembershipResponse;
import com.app.backend.domain.group.repository.GroupMembershipRepository;
import com.app.backend.domain.member.dto.request.MemberLoginRequestDto;
import com.app.backend.domain.member.dto.request.MemberModifyRequestDto;
import com.app.backend.domain.member.dto.response.MemberJoinResponseDto;
import com.app.backend.domain.member.dto.response.MemberLoginResponseDto;
import com.app.backend.domain.member.dto.response.MemberModifyResponseDto;
import com.app.backend.domain.member.entity.Member;
import com.app.backend.domain.member.exception.MemberErrorCode;
import com.app.backend.domain.member.exception.MemberException;
import com.app.backend.domain.member.jwt.JwtProvider;
import com.app.backend.domain.member.repository.MemberRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@Transactional(readOnly = true)
public class MemberService {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(MemberService.class);
    
    private final MemberRepository memberRepository;
    private final GroupMembershipRepository groupMembershipRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtProvider jwtProvider;
    private final boolean disabled = false;

    public MemberService(MemberRepository memberRepository,
                        GroupMembershipRepository groupMembershipRepository,
                        PasswordEncoder passwordEncoder,
                        JwtProvider jwtProvider) {
        this.memberRepository = memberRepository;
        this.groupMembershipRepository = groupMembershipRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtProvider = jwtProvider;
    }

    @Transactional
    public MemberJoinResponseDto createMember(String username, String password, String nickname) {
        memberRepository.findByUsernameAndDisabled(username, disabled)
                .ifPresent(a -> {
                    throw new MemberException(MemberErrorCode.MEMBER_USERNAME_EXISTS);
                });
        memberRepository.findByNicknameAndDisabled(nickname, disabled)
                .ifPresent(a -> {
                    throw new MemberException(MemberErrorCode.MEMBER_NICKNAME_EXISTS);
                });

        Member member = Member.create(
                username,
                passwordEncoder.encode(password),
                nickname,
                "ROLE_ADMIN",
                false,
                null,
                null
        );

        Member savedMember = memberRepository.save(member);

        // Entity -> Response DTO 변환
        return MemberJoinResponseDto.from(savedMember);
    }

    public MemberLoginResponseDto login(MemberLoginRequestDto request) {
        Member member = memberRepository.findByUsernameAndDisabled(request.getUsername(), disabled)
                .orElseThrow(() -> new MemberException(MemberErrorCode.MEMBER_NOT_FOUND));

        if (!passwordEncoder.matches(request.getPassword(), member.getPassword()))
            throw new MemberException(MemberErrorCode.MEMBER_PASSWORD_NOT_MATCH);

        // 토큰 생성
        String accessToken = jwtProvider.generateAccessToken(member);
        String refreshToken = jwtProvider.generateRefreshToken();

        memberRepository.save(member);

        // 응답
        return MemberLoginResponseDto.Companion.of(member, accessToken, refreshToken);
    }

    @Transactional
    public void logout(String token) {
        try {
            Member member = getCurrentMember(token);
            memberRepository.save(member);
        } catch (Exception e) {
            throw new MemberException(MemberErrorCode.MEMBER_FAILED_LOGOUT);
        }
    }

    public Member getCurrentMember(String accessToken) {
        return Optional.ofNullable(accessToken)
                .map(t -> t.startsWith("Bearer ") ? t.substring(7) : t)
                .filter(jwtProvider::validateToken)
                .map(validateToken -> {
                    Long memberId = jwtProvider.getMemberId(validateToken);
                    return this.memberRepository.findByIdAndDisabled(memberId, false)
                            .orElseThrow(() -> new MemberException(
                                    MemberErrorCode.MEMBER_NOT_FOUND));
                })
                .orElseThrow(() -> new MemberException(MemberErrorCode.MEMBER_UNVALID_TOKEN));  // 토큰 검증
    }

    @Transactional
    public MemberModifyResponseDto modifyMember(Member member, MemberModifyRequestDto request) {
        Member modifiedMember = Member.create(
                member.getUsername(),
                request.password() != null ? passwordEncoder.encode(request.password()) : member.getPassword(),
                member.getNickname(),
                member.getRole(),
                member.isDisabled(),
                member.getProvider(),
                member.getOauthProviderId()
        );

        Member savedMember = Optional.of(
                memberRepository.save(modifiedMember)
        ).orElseThrow(() -> new MemberException(MemberErrorCode.MEMBER_FAILED_TO_MODIFY));

        return MemberModifyResponseDto.of(savedMember);
    }

    public Optional<List<Member>> findAllMembers(String token) {
        return Optional.ofNullable(token)
                .map(t -> t.startsWith("Bearer ") ? t.substring(7) : t)
                .filter(jwtProvider::validateToken)
                .filter(validateToken -> {
                    String role = jwtProvider.getRole(validateToken);
                    if (!role.contains("ADMIN"))
                        throw new MemberException(MemberErrorCode.MEMBER_NO_ADMIN_PERMISSION);
                    return true;
                })
                .map(validateToken -> memberRepository.findAllByOrderByIdDesc());
    }

    @Transactional
    public void deleteMember(String token) {
        Member member = getCurrentMember(token);
        member = Member.create(
                member.getUsername(),
                member.getPassword(),
                member.getNickname(),
                member.getRole(),
                member.isDisabled(),
                member.getProvider(),
                member.getOauthProviderId()
        );

        memberRepository.save(member);
    }

    @Transactional
    @Scheduled(fixedRate = 60000 * 30) // 30분마다 실행
    public void cleanupDisabledMembers() {
        log.info("비활성화된 회원 정보 삭제 작업 시작");
        LocalDateTime cutoffDate = LocalDateTime.now().minusSeconds(30);
        int deletedCount = memberRepository.deleteByDisabledIsTrueAndModifiedAtLessThan(cutoffDate);
        log.info("삭제된 회원 수: {}", deletedCount);
    }

    public List<GroupMembershipResponse.Detail> getMyGroup(String token) {
        return Optional.ofNullable(token)
                .map(t -> t.startsWith("Bearer ") ? t.substring(7) : t)
                .filter(jwtProvider::validateToken)
                .map(validateToken -> {
                    Long id = jwtProvider.getMemberId(validateToken);
                    return groupMembershipRepository.findAllByMemberIdAndDisabled(id, false)
                            .stream()
                            .map(GroupMembershipResponse::toDetail)
                            .toList();
                })
                .orElse(List.of());  // 토큰이 없거나 유효하지 않은 경우 빈 리스트 반환
    }
}
