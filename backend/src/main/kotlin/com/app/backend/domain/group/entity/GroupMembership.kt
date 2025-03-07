package com.app.backend.domain.group.entity

import com.app.backend.domain.member.entity.Member
import com.app.backend.global.entity.BaseEntity
import jakarta.persistence.*

@Entity
@Table(name = "tbl_group_memberships")
@IdClass(GroupMembershipId::class)
class GroupMembership private constructor(
    member: Member,
    group: Group,
    groupRole: GroupRole,
    status: MembershipStatus
) : BaseEntity() {
    companion object {
        fun of(
            member: Member,
            group: Group,
            groupRole: GroupRole
        ) = GroupMembership(
            member,
            group,
            groupRole,
            if (groupRole == GroupRole.LEADER) MembershipStatus.APPROVED else MembershipStatus.PENDING
        )
    }

    init {
        setRelationshipWithMember(member)
        setRelationshipWithGroup(group)
    }

    @Id
    @Column(name = "member_id", insertable = false, updatable = false)
    var memberId: Long? = null
        protected set

    @Id
    @Column(name = "group_id", insertable = false, updatable = false)
    var groupId: Long? = null
        protected set

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", insertable = false, updatable = false)
    var member: Member = member
        protected set

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "group_id", insertable = false, updatable = false)
    var group: Group = group
        protected set

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var groupRole: GroupRole = groupRole
        protected set

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var status: MembershipStatus = status
        protected set

    //==================== 연관관계 함수 ====================//

    private fun setRelationshipWithMember(member: Member) {
        memberId = member.id
        this.member = member
        //TODO: 필요 시, 회원(Member)과의 연관관계 설정 추가
    }

    private fun setRelationshipWithGroup(group: Group) {
        groupId = group.id
        this.group = group
        group.members.add(this)
    }

    //==================== 모임 멤버십(GroupMembership) 수정 함수 ====================//

    /**
     * 모임 내 회원 권한 수정
     *
     * @param newGroupRole - 새로운 모임 내 권한
     * @return this
     */
    fun modifyGroupRole(newGroupRole: GroupRole) = apply {
        if (this.groupRole == newGroupRole) return@apply
        this.groupRole = newGroupRole
    }

    /**
     * 모임 내 회원 상태 수정
     *
     * @param newMembershipStatus - 새로운 멤버십 상태
     * @return this
     */
    fun modifyStatus(newMembershipStatus: MembershipStatus) = apply {
        if (this.status == newMembershipStatus) return@apply
        this.status = newMembershipStatus
    }
}
