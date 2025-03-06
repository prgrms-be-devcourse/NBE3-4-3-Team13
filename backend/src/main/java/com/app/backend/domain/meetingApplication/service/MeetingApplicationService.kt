package com.app.backend.domain.meetingApplication.service

import com.app.backend.domain.group.entity.GroupMembership
import com.app.backend.domain.group.entity.GroupRole
import com.app.backend.domain.group.entity.MembershipStatus
import com.app.backend.domain.group.exception.GroupMembershipErrorCode
import com.app.backend.domain.group.exception.GroupMembershipException
import com.app.backend.domain.group.repository.GroupMembershipRepository
import com.app.backend.domain.group.repository.GroupRepository
import com.app.backend.domain.meetingApplication.dto.MeetingApplicationReqBody
import com.app.backend.domain.meetingApplication.dto.response.MeetingApplicationResponse
import com.app.backend.domain.chat.room.controller.MeetingApplication
import com.app.backend.domain.meetingApplication.exception.MeetingApplicationErrorCode
import com.app.backend.domain.meetingApplication.exception.MeetingApplicationException
import com.app.backend.domain.meetingApplication.repository.MeetingApplicationRepository
import com.app.backend.domain.member.repository.MemberRepository
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional(readOnly = true)
class MeetingApplicationService(
    private val meetingApplicationRepository: MeetingApplicationRepository,
    private val groupRepository: GroupRepository,
    private val memberRepository: MemberRepository,
    private val groupMembershipRepository: GroupMembershipRepository
) {

    @Transactional
    fun create(groupId: Long, request: MeetingApplicationReqBody, memberId: Long?): MeetingApplication {
        val group = groupRepository.findByIdAndDisabled(groupId, false)
            .orElseThrow { MeetingApplicationException(MeetingApplicationErrorCode.GROUP_NOT_FOUND) }
        val member = memberRepository.findByIdAndDisabled(memberId, false)
            .orElseThrow { MeetingApplicationException(MeetingApplicationErrorCode.MEMBER_NOT_FOUND) }

        groupMembershipRepository.findByGroupIdAndMemberIdAndDisabled(groupId, memberId, false)
            .ifPresentOrElse(
                { existingMembership ->
                    if (existingMembership.status == MembershipStatus.REJECTED || existingMembership.status == MembershipStatus.LEAVE) {
                        existingMembership.modifyStatus(MembershipStatus.PENDING)
                        groupMembershipRepository.save(existingMembership)
                    } else {
                        throw MeetingApplicationException(MeetingApplicationErrorCode.ALREADY_IN_GROUP)
                    }
                },
                {
                    groupMembershipRepository.save(
                        GroupMembership.of(
                            member = member,
                            group = group,
                            groupRole = GroupRole.PARTICIPANT
                        )
                    )
                }
            )

        return meetingApplicationRepository
            .findByGroup_IdAndMember_IdAndDisabled(groupId, memberId, false)
            .orElseGet {
                meetingApplicationRepository.save(
                    MeetingApplication(
                        context = request.context,
                        group = group,
                        member = member
                    )
                )
            }
            .modifyContext(request.context)
    }

    fun validateGroupMemberLimit(groupId: Long) {
        val group = groupRepository.findByIdAndDisabled(groupId, false)
            .orElseThrow { MeetingApplicationException(MeetingApplicationErrorCode.GROUP_NOT_FOUND) }

        val approvedMemberCount = groupMembershipRepository.countByGroupIdAndStatusAndDisabled(groupId, MembershipStatus.APPROVED, false)

        if (approvedMemberCount >= group.maxRecruitCount) {
            throw MeetingApplicationException(MeetingApplicationErrorCode.GROUP_MEMBER_LIMIT_EXCEEDED)
        }
    }

    fun getMeetingApplicationById(groupId: Long, meetingApplicationId: Long, memberId: Long?): MeetingApplicationResponse.Detail {
        val groupLeaderMembership = groupMembershipRepository.findByGroupIdAndMemberIdAndDisabled(groupId, memberId, false)
            .orElseThrow { GroupMembershipException(GroupMembershipErrorCode.GROUP_MEMBERSHIP_NOT_FOUND) }

        if (groupLeaderMembership.status != MembershipStatus.APPROVED || groupLeaderMembership.groupRole != GroupRole.LEADER) {
            throw GroupMembershipException(GroupMembershipErrorCode.GROUP_MEMBERSHIP_NO_PERMISSION)
        }

        val meetingApplication = meetingApplicationRepository
            .findByIdAndDisabled(meetingApplicationId, false)
            .orElseThrow { MeetingApplicationException(MeetingApplicationErrorCode.MEETING_APPLICATION_NOT_FOUND) }

        val groupMembership = groupMembershipRepository
            .findByGroupIdAndMemberIdAndDisabled(groupId, meetingApplication.member.id!!, false)
            .orElseThrow { GroupMembershipException(GroupMembershipErrorCode.GROUP_MEMBERSHIP_NOT_FOUND) }

        return MeetingApplicationResponse.toDetail(
            meetingApplication,
            groupMembership.status == MembershipStatus.REJECTED,
            groupMembership.status == MembershipStatus.APPROVED,
            groupMembership.groupRole == GroupRole.LEADER
        )
    }

    fun getMeetingApplications(groupId: Long, memberId: Long?, pageable: Pageable): Page<MeetingApplicationResponse.Detail> {
        val groupMembership = groupMembershipRepository.findByGroupIdAndMemberIdAndDisabled(groupId, memberId, false)
            .orElseThrow { GroupMembershipException(GroupMembershipErrorCode.GROUP_MEMBERSHIP_NOT_FOUND) }

        if (groupMembership.status != MembershipStatus.APPROVED || groupMembership.groupRole != GroupRole.LEADER) {
            throw GroupMembershipException(GroupMembershipErrorCode.GROUP_MEMBERSHIP_NO_PERMISSION)
        }

        val list = meetingApplicationRepository.findByGroupIdAndDisabled(groupId, false)
        val map = groupMembershipRepository.findAllByGroupIdAndDisabled(groupId, false)
            .associateBy { it.memberId }

        val content = list.map { ma ->
            val gm = map[ma.member.id]
            val rejected = gm?.status == MembershipStatus.REJECTED
            val isMember = gm?.status == MembershipStatus.APPROVED
            val isAdmin = isMember && gm?.groupRole == GroupRole.LEADER
            MeetingApplicationResponse.toDetail(ma, rejected, isMember, isAdmin)
        }
            .sortedByDescending { it.createdAt }

        return PageImpl(content, pageable, content.size.toLong())
    }
}
