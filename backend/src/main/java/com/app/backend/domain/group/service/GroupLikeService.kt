package com.app.backend.domain.group.service

import com.app.backend.domain.group.entity.GroupLike
import com.app.backend.domain.group.exception.GroupLikeErrorCode
import com.app.backend.domain.group.exception.GroupLikeException
import com.app.backend.domain.group.repository.GroupLikeRepository
import com.app.backend.domain.group.repository.GroupRepository
import com.app.backend.domain.member.repository.MemberRepository
import jakarta.persistence.EntityManager
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional(readOnly = true)
class GroupLikeService(
    private val groupLikeRepository: GroupLikeRepository,
    private val groupRepository: GroupRepository,
    private val memberRepository: MemberRepository,
    private val entityManager: EntityManager
) {
    /** 그룹 좋아요 여부 확인 */
    fun isLiked(groupId: Long, memberId: Long): Boolean =
        groupLikeRepository.findByGroupAndMember(
            groupRepository.findById(groupId).orElseThrow { GroupLikeException(GroupLikeErrorCode.GROUP_NOT_FOUND) },
            memberRepository.findById(memberId).orElseThrow { GroupLikeException(GroupLikeErrorCode.MEMBER_NOT_FOUND) }
        ).isPresent

    /** 그룹 좋아요 추가 */
    @Transactional
    fun likeGroup(groupId: Long, memberId: Long) {
        val group = groupRepository.findByIdWithLock(groupId)
            .orElseThrow { GroupLikeException(GroupLikeErrorCode.GROUP_NOT_FOUND) }
        val member = memberRepository.findById(memberId)
            .orElseThrow { GroupLikeException(GroupLikeErrorCode.MEMBER_NOT_FOUND) }

        if (groupLikeRepository.findByGroupAndMember(group, member).isPresent)
            throw GroupLikeException(GroupLikeErrorCode.ALREADY_LIKED)

        groupLikeRepository.save(GroupLike(member, group))
        entityManager.flush()
        group.increaseLikeCount()
    }

    /** 그룹 좋아요 취소 */
    @Transactional
    fun unlikeGroup(groupId: Long, memberId: Long) {
        val group = groupRepository.findByIdWithLock(groupId)
            .orElseThrow { GroupLikeException(GroupLikeErrorCode.GROUP_NOT_FOUND) }
        val member = memberRepository.findById(memberId)
            .orElseThrow { GroupLikeException(GroupLikeErrorCode.MEMBER_NOT_FOUND) }

        val existingLike = groupLikeRepository.findByGroupAndMember(group, member)
            .orElseThrow { GroupLikeException(GroupLikeErrorCode.NOT_LIKED_YET) }

        groupLikeRepository.delete(existingLike)
        group.decreaseLikeCount()
    }
}