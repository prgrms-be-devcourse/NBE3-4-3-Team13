package com.app.backend.domain.meetingApplication.dto

import com.app.backend.domain.meetingApplication.entity.MeetingApplication

data class MeetingApplicationDto(
    val id: Long,
    val memberId: Long,
    val groupId: Long,
    val context: String
) {
    companion object {
        operator fun invoke(meetingApplication: MeetingApplication) = MeetingApplicationDto(
            meetingApplication.id ?: throw IllegalArgumentException("MeetingApplication ID cannot be null"),
            meetingApplication.member.id ?: throw IllegalStateException("Member ID cannot be null"),
            meetingApplication.group.id ?: throw IllegalStateException("Group ID cannot be null"),
            meetingApplication.context
        )
    }
}
