package com.app.backend.domain.member.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.app.backend.domain.member.entity.Member;

public interface MemberRepository extends JpaRepository<Member, Long> {
    Optional<Member> findByIdAndDisabled(Long in, boolean disabled);

    Optional<Member> findByUsernameAndDisabled(String username, boolean disabled);

    Optional<Member> findByNicknameAndDisabled(String nickname, boolean disabled);

    Optional<Member> findByOauthProviderId(String oauthProviderId);

    int deleteByDisabledIsTrueAndModifiedAtLessThan(LocalDateTime modifiedAt);

    List<Member> findAllByOrderByIdDesc();
}
