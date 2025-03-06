package com.app.backend.domain.group.service

import com.app.backend.domain.group.entity.GroupRole
import com.app.backend.domain.group.entity.MembershipStatus
import com.app.backend.domain.group.entity.RecruitStatus
import com.app.backend.domain.group.exception.GroupErrorCode
import com.app.backend.domain.group.exception.GroupException
import com.app.backend.domain.group.exception.GroupMembershipErrorCode
import com.app.backend.domain.group.exception.GroupMembershipException
import com.app.backend.domain.group.repository.GroupMembershipRepository
import com.app.backend.domain.group.repository.GroupRepository
import com.app.backend.domain.notification.dto.NotificationEvent
import com.app.backend.domain.notification.service.NotificationService
import com.app.backend.global.annotation.CustomLock
import jakarta.validation.constraints.Min
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional(readOnly = true)
class GroupMembershipService(
    private val notificationService: NotificationService,
    private val groupMembershipRepository: GroupMembershipRepository,
    private val groupRepository: GroupRepository
) {
    /**
     * 모임 가입 신청을 승인 또는 거절
     *
     * @param groupLeaderId - 모임 관리자 ID
     * @param groupId       - 모임 ID
     * @param memberId      - 모임 가입 신청 회원 ID
     * @param isAccept      - 가입 승인 여부
     * @return 모임 가입 승인 여부
     */
    @CustomLock(key = "'group:' + #groupId + '-member:' + #memberId")
    @Transactional
    fun approveJoining(
        @Min(1) groupLeaderId: Long,
        @Min(1) groupId: Long,
        @Min(1) memberId: Long,
        isAccept: Boolean
    ): Boolean {
        val group = groupRepository.findByIdAndDisabled(groupId, false)
            .orElseThrow { GroupException(GroupErrorCode.GROUP_NOT_FOUND) }

        //대상 모임이 모집 상태가 아닐 경우 예외(= 모집이 닫혀있는 경우 예외)
        if (group.recruitStatus != RecruitStatus.RECRUITING)
            throw GroupException(GroupErrorCode.GROUP_NOT_IN_RECRUITMENT_STATUS)

        //대상 모임 내 가입 회원 수 조회
        val count = groupMembershipRepository.countByGroupIdAndStatusAndDisabled(
            groupId,
            MembershipStatus.APPROVED,
            false
        )

        //모임 최대 가입 한도와 가입 회원 수 비교, 이미 최대 가입 한도에 도달한 경우 예외
        if (group.maxRecruitCount <= count)
            throw GroupException(GroupErrorCode.GROUP_MAXIMUM_NUMBER_OF_MEMBERS)

        //모임 가입 신청을 승인하려는 회원이 해당 모임의 관리자(LEADER) 권한을 갖고 있는지 확인
        val groupLeaderMembership = groupMembershipRepository.findByGroupIdAndMemberIdAndDisabled(
            groupId,
            groupLeaderId,
            false
        ).orElseThrow { GroupMembershipException(GroupMembershipErrorCode.GROUP_MEMBERSHIP_NOT_FOUND) }
        if (groupLeaderMembership.groupRole != GroupRole.LEADER)
            throw GroupMembershipException(GroupMembershipErrorCode.GROUP_MEMBERSHIP_UNACCEPTABLE_STATUS)

        //모임 가입을 신청한 회원의 멤버십을 조회
        val groupMembership = groupMembershipRepository.findByGroupIdAndMemberIdAndDisabled(
            groupId,
            memberId,
            false
        ).orElseThrow { GroupMembershipException(GroupMembershipErrorCode.GROUP_MEMBERSHIP_NOT_FOUND) }

        //멤버십 상태가 미승인(PENDING) 또는 거절(REJECTED) 상태가 아닐 경우 예외(= 모임에 가입된 상태(APPROVED) 또는 탈퇴(LEAVE)한 상태의 경우 예외 발생)
        if (groupMembership.status == MembershipStatus.APPROVED || groupMembership.status == MembershipStatus.LEAVE)
            throw GroupMembershipException(GroupMembershipErrorCode.GROUP_MEMBERSHIP_UNACCEPTABLE_STATUS)

        //모임의 관리자 권한을 갖는 회원이 가입을 승인한 경우(isAccept = true)
        if (isAccept) {
            groupMembership.modifyStatus(MembershipStatus.APPROVED)
            notificationService.sendNotification(
                memberId.toString(),
                "그룹 가입 승인",
                group.name + " 그룹 가입이 승인되었습니다",
                NotificationEvent.NotificationType.GROUP_INVITE,
                group.id!!
            )

            if (
                group.maxRecruitCount <= groupMembershipRepository.countByGroupIdAndStatusAndDisabled(
                    groupId,
                    MembershipStatus.APPROVED,
                    false
                )
            )
                group.modifyRecruitStatus(RecruitStatus.CLOSED.apply { modifyForceStatus(false) })

            return true
        }

        //모임의 관리자 권한을 갖는 회원이 가입을 승인하지 않은 경우(isAccept = false)
        groupMembership.modifyStatus(MembershipStatus.REJECTED)
        return false
    }

    /**
     * 모임 내 회원의 권한을 변경
     *
     * @param groupLeaderId - 모임 관리자 ID
     * @param groupId       - 모임 ID
     * @param memberId      - 모임 내 권한 변경 대상 회원 ID
     * @return 권한 변경 성공 여부
     */
    @CustomLock(key = "'group:' + #groupId + '-member:' + #memberId")
    @Transactional
    fun modifyGroupRole(@Min(1) groupLeaderId: Long, @Min(1) groupId: Long, @Min(1) memberId: Long): Boolean {
        //모임 내 회원의 권한을 변경하려는 회원이 해당 모임의 관리자(LEADER) 권한을 갖고 있는지 확인
        val groupLeaderMembership = groupMembershipRepository.findByGroupIdAndMemberIdAndDisabled(
            groupId,
            groupLeaderId,
            false
        ).orElseThrow { GroupMembershipException(GroupMembershipErrorCode.GROUP_MEMBERSHIP_NOT_FOUND) }
        if (groupLeaderMembership.groupRole != GroupRole.LEADER)
            throw GroupMembershipException(GroupMembershipErrorCode.GROUP_MEMBERSHIP_NO_PERMISSION)

        //모임 내 권한을 변경하려는 회원 멤버십 조회
        val groupMembership = groupMembershipRepository.findByGroupIdAndMemberIdAndDisabled(
            groupId,
            memberId,
            false
        ).orElseThrow { GroupMembershipException(GroupMembershipErrorCode.GROUP_MEMBERSHIP_NOT_FOUND) }

        //멤버십이 가입 상태(APPROVED)가 아닐 경우 예외(= 미승인(PENDING) 또는 거절(REJECTED) 또는 탈퇴(LEAVE)일 경우 예외)
        if (groupMembership.status != MembershipStatus.APPROVED)
            throw GroupMembershipException(GroupMembershipErrorCode.GROUP_MEMBERSHIP_GROUP_ROLE_NOT_CHANGEABLE_STATE)

        //회원의 모임 내 권한을 변경: LEADER <-> PARTICIPANT
        if (groupMembership.groupRole == GroupRole.LEADER)
            groupMembership.modifyGroupRole(GroupRole.PARTICIPANT)
        else
            groupMembership.modifyGroupRole(GroupRole.LEADER)

        return true
    }

    /**
     * 모임에서 탈퇴
     *
     * @param groupId  - 모임 ID
     * @param memberId - 회원 ID
     * @return 탈퇴 성공 여부
     */
    @CustomLock(key = "'group:' + #groupId + '-member:' + #memberId")
    @Transactional
    fun leaveGroup(@Min(1) groupId: Long, @Min(1) memberId: Long): Boolean {
        val groupMembership = groupMembershipRepository.findByGroupIdAndMemberIdAndDisabled(
            groupId,
            memberId,
            false
        ).orElseThrow { GroupMembershipException(GroupMembershipErrorCode.GROUP_MEMBERSHIP_NOT_FOUND) }

        //모임을 탈퇴하기 전 모임 내 관리자 권한을 갖는 회원의 숫자 조회
        val groupLeaderCount = groupMembershipRepository.countByGroupIdAndGroupRoleAndDisabled(
            groupId,
            GroupRole.LEADER,
            false
        )

        //탈퇴하려는 회원이 관리자 권한을 갖고 있으며, 해당 모임 내 관리자 권한의 회원이 1명 이하인 경우 예외 발생, 탈퇴가 성공하려면 탈퇴 후 모임의 관리자가 1명 이상 존재해야 함
        if (groupMembership.groupRole == GroupRole.LEADER && groupLeaderCount <= 1)
            throw GroupMembershipException(GroupMembershipErrorCode.GROUP_MEMBERSHIP_UNABLE_TO_LEAVE)

        groupMembership.modifyStatus(MembershipStatus.LEAVE)

        val group = groupMembership.group
        if (
            group.maxRecruitCount > groupMembershipRepository.countByGroupIdAndStatusAndDisabled(
                groupId,
                MembershipStatus.APPROVED,
                false
            )
            && !group.recruitStatus.forceStatus
        )
            group.modifyRecruitStatus(RecruitStatus.RECRUITING)

        return true
    }
}