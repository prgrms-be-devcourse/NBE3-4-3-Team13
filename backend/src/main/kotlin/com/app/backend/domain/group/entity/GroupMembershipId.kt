package com.app.backend.domain.group.entity

import java.io.Serializable

class GroupMembershipId() : Serializable {
    constructor(memberId: Long, groupId: Long) : this() {
        this.memberId = memberId
        this.groupId = groupId
    }

    var memberId: Long? = null
        protected set
    var groupId: Long? = null
        protected set

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is GroupMembershipId) return false
        return memberId == other.memberId && groupId == other.groupId
    }

    override fun hashCode(): Int = (memberId?.hashCode() ?: 0) * 31 + (groupId?.hashCode() ?: 0)
}