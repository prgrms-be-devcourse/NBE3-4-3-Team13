package com.app.backend.domain.member.repository

import com.app.backend.domain.member.entity.Member
import org.springframework.data.jpa.repository.JpaRepository
import java.time.LocalDateTime
import java.util.*

interface MemberRepository : JpaRepository<Member, Long> {
    fun findByIdAndDisabled(id: Long, disabled: Boolean): Optional<Member>

    fun findByUsernameAndDisabled(username: String, disabled: Boolean): Optional<Member>

    fun findByNicknameAndDisabled(nickname: String, disabled: Boolean): Optional<Member>

    fun findByOauthProviderId(oauthProviderId: String): Optional<Member>

    fun deleteByDisabledIsTrueAndModifiedAtLessThan(modifiedAt: LocalDateTime): Int

    fun findAllByOrderByIdDesc(): List<Member>
}