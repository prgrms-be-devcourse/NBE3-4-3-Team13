package com.app.backend.domain.meetingApplication.repository

import com.app.backend.domain.meetingApplication.entity.MeetingApplication
import org.springframework.data.jpa.repository.JpaRepository
import java.util.Optional

interface MeetingApplicationRepository : JpaRepository<MeetingApplication, Long> {

    fun findByGroupIdAndDisabled(groupId: Long, disabled: Boolean): List<MeetingApplication>

    fun findByGroupIdAndIdAndDisabled(
        groupId: Long,
        meetingApplicationId: Long,
        disabled: Boolean
    ): Optional<MeetingApplication>

    fun findByIdAndDisabled(id: Long, disabled: Boolean): Optional<MeetingApplication>

    fun findByGroup_IdAndMember_IdAndDisabled(
        groupId: Long,
        memberId: Long?,
        disabled: Boolean
    ): Optional<MeetingApplication>
}
