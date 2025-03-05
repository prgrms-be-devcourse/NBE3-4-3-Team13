package com.app.backend.domain.group.repository

import com.app.backend.domain.group.entity.GroupMembership
import com.app.backend.domain.group.entity.GroupMembershipId
import com.app.backend.domain.group.entity.GroupRole
import com.app.backend.domain.group.entity.MembershipStatus
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.util.*

interface GroupMembershipRepository : JpaRepository<GroupMembership, GroupMembershipId>,
    GroupMembershipRepositoryCustom {
    fun findByGroupIdAndMemberId(groupId: Long, memberId: Long): Optional<GroupMembership>
    fun findByGroupIdAndMemberIdAndDisabled(groupId: Long, memberId: Long, disabled: Boolean): Optional<GroupMembership>
    fun findAllByGroupId(groupId: Long): List<GroupMembership>
    fun findAllByGroupIdAndDisabled(groupId: Long, disabled: Boolean): List<GroupMembership>
    fun findAllByGroupRole(groupRole: GroupRole): List<GroupMembership>
    fun findAllByGroupRoleAndDisabled(groupRole: GroupRole, disabled: Boolean): List<GroupMembership>
    fun findAllByGroupIdAndGroupRole(groupId: Long, groupRole: GroupRole): List<GroupMembership>

    fun findAllByGroupIdAndGroupRoleAndDisabled(
        groupId: Long,
        groupRole: GroupRole,
        disabled: Boolean
    ): List<GroupMembership>

    fun findAllByMemberIdAndGroupRole(memberId: Long, groupRole: GroupRole): List<GroupMembership>

    fun findAllByMemberIdAndGroupRoleAndDisabled(
        memberId: Long,
        groupRole: GroupRole,
        disabled: Boolean
    ): List<GroupMembership>

    fun existsByGroupIdAndMemberId(groupId: Long, memberId: Long): Boolean
    fun existsByGroupIdAndMemberIdAndDisabled(groupId: Long, memberId: Long, disabled: Boolean): Boolean
    fun countByGroupIdAndGroupRole(groupId: Long, groupRole: GroupRole): Int
    fun countByGroupIdAndGroupRoleAndDisabled(groupId: Long, groupRole: GroupRole, disabled: Boolean): Int
    fun countByGroupIdAndGroupRoleIn(groupId: Long, groupRoles: Set<GroupRole>): Int
    fun countByGroupIdAndGroupRoleInAndDisabled(groupId: Long, groupRoles: Set<GroupRole>, disabled: Boolean): Int
    fun countByGroupIdAndStatus(groupId: Long, status: MembershipStatus): Int
    fun countByGroupIdAndStatusAndDisabled(groupId: Long, status: MembershipStatus, disabled: Boolean): Int

    @Modifying
    @Query("UPDATE GroupMembership g SET g.disabled = :disabled WHERE g.groupId = :groupId")
    fun updateDisabledForAllGroupMembership(@Param("groupId") groupId: Long, @Param("disabled") disabled: Boolean): Int
}