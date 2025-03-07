package com.app.backend.domain.group.dto.response

import com.app.backend.domain.group.entity.GroupMembership
import com.app.backend.domain.group.entity.GroupRole
import com.app.backend.domain.group.entity.MembershipStatus
import com.app.backend.global.util.AppUtil
import com.fasterxml.jackson.annotation.JsonProperty

class GroupMembershipResponse {
    companion object {
        fun toDetail(groupMembership: GroupMembership) = Detail(
            groupId = groupMembership.groupId!!,
            categoryName = groupMembership.group.category.name,
            name = groupMembership.group.name,
            modifiedAt = AppUtil.localDateTimeToString(groupMembership.modifiedAt),
            isApplying = if (groupMembership.status == MembershipStatus.PENDING) true else null,
            isRejected = if (groupMembership.status == MembershipStatus.REJECTED) true else null,
            isMember = if (groupMembership.status == MembershipStatus.APPROVED) true else null,
            isAdmin = groupMembership.status == MembershipStatus.APPROVED && groupMembership.groupRole == GroupRole.LEADER
        )
    }

    data class Detail(
        val groupId: Long,
        val categoryName: String,
        val name: String,
        val modifiedAt: String,
        @get:JsonProperty("isApplying") val isApplying: Boolean?,
        @get:JsonProperty("isRejected") val isRejected: Boolean?,
        @get:JsonProperty("isMember") val isMember: Boolean?,
        @get:JsonProperty("isAdmin") val isAdmin: Boolean?
    )
}