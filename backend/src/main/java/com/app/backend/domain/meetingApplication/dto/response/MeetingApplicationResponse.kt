package com.app.backend.domain.meetingApplication.dto.response

import com.app.backend.domain.chat.room.controller.MeetingApplication
import com.app.backend.global.util.AppUtil

object MeetingApplicationResponse {
    fun toDetail(
        meetingApplication: MeetingApplication,
        rejected: Boolean,
        isMember: Boolean,
        isAdmin: Boolean
    ): Detail {
        return Detail(
            id = meetingApplication.id ?: throw IllegalStateException("MeetingApplication ID cannot be null"),
            groupId = meetingApplication.group.id ?: throw IllegalStateException("Group ID cannot be null"),
            memberId = meetingApplication.member.id ?: throw IllegalStateException("Member ID cannot be null"),
            nickname = meetingApplication.member.nickname ?: throw IllegalStateException("Member or nickname cannot be null"),
            content = meetingApplication.context,
            createdAt = AppUtil.localDateTimeToString(meetingApplication.createdAt),
            rejected = rejected,
            isMember = isMember,
            isAdmin = isAdmin
        )
    }

    data class Detail(
        val id: Long,
        val groupId: Long,
        val memberId: Long,
        val nickname: String,
        val content: String,
        val createdAt: String,
        val rejected: Boolean,
        val isMember: Boolean,
        val isAdmin: Boolean
    )
}
