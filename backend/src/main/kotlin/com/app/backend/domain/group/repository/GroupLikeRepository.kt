package com.app.backend.domain.group.repository

import com.app.backend.domain.group.entity.Group
import com.app.backend.domain.group.entity.GroupLike
import com.app.backend.domain.member.entity.Member
import jakarta.persistence.LockModeType
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Lock
import org.springframework.data.jpa.repository.Query
import java.util.*

interface GroupLikeRepository : JpaRepository<GroupLike, Long>, GroupLikeRepositoryCustom {
    fun countByGroupIdAndMemberId(groupId: Long, memberId: Long): Long

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT gl FROM GroupLike gl WHERE gl.group = :group AND gl.member = :member")
    fun findByGroupAndMember(group: Group, member: Member): Optional<GroupLike>
}