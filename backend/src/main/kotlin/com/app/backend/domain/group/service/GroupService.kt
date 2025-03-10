package com.app.backend.domain.group.service

import com.app.backend.domain.category.exception.CategoryErrorCode
import com.app.backend.domain.category.exception.CategoryException
import com.app.backend.domain.category.repository.CategoryRepository
import com.app.backend.domain.chat.room.entity.ChatRoom
import com.app.backend.domain.chat.room.repository.ChatRoomRepository
import com.app.backend.domain.group.dto.request.GroupRequest
import com.app.backend.domain.group.dto.response.GroupResponse
import com.app.backend.domain.group.entity.*
import com.app.backend.domain.group.exception.GroupErrorCode
import com.app.backend.domain.group.exception.GroupException
import com.app.backend.domain.group.exception.GroupMembershipErrorCode
import com.app.backend.domain.group.exception.GroupMembershipException
import com.app.backend.domain.group.repository.GroupLikeRepository
import com.app.backend.domain.group.repository.GroupMembershipRepository
import com.app.backend.domain.group.repository.GroupRepository
import com.app.backend.domain.member.exception.MemberErrorCode
import com.app.backend.domain.member.exception.MemberException
import com.app.backend.domain.member.repository.MemberRepository
import com.app.backend.global.annotation.CustomLock
import jakarta.persistence.EntityManager
import jakarta.persistence.PersistenceContext
import jakarta.validation.constraints.Min
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional(readOnly = true)
class GroupService(
    @PersistenceContext private val entityManager: EntityManager,
    private val groupRepository: GroupRepository,
    private val groupMembershipRepository: GroupMembershipRepository,
    private val memberRepository: MemberRepository,
    private val chatRoomRepository: ChatRoomRepository,
    private val categoryRepository: CategoryRepository,
    private val groupLikeService: GroupLikeService,
    private val groupLikeRepository: GroupLikeRepository
) {
    /**
     * 모임(Group) 저장
     *
     * @param memberId - 회원 ID
     * @param dto      - 모임(Group) 생성 요청 DTO
     * @return 생성된 Group 엔티티 ID
     */
    @Transactional
    fun createGroup(memberId: Long, dto: GroupRequest.Create): Long {
        //모임을 생성하는 회원 조회
        val member = memberRepository.findByIdAndDisabled(memberId, false)
            .orElseThrow { MemberException(MemberErrorCode.MEMBER_NOT_FOUND) }

        //생성할 모임을 추가할 카테고리 조회
        val category = categoryRepository.findByNameAndDisabled(dto.categoryName, false)
            .orElseThrow { CategoryException(CategoryErrorCode.CATEGORY_NOT_FOUND) }

        //모임 엔티티 생성
        val group = Group.of(
            dto.name,
            dto.province,
            dto.city,
            dto.town,
            dto.description,
            RecruitStatus.RECRUITING,
            dto.maxRecruitCount,
            category
        )

        //모임 채팅방 엔티티 생성
        val chatRoom = ChatRoom(group)
        group.chatRoom = chatRoom
        chatRoomRepository.save(chatRoom)
        val groupId = groupRepository.save(group).id!!

        //모임 멤버십 엔티티 생성(회원-모임 연결 테이블, 모임 관리자 권한(LEADER) 부여)
        val groupMembership = GroupMembership.of(member, group, GroupRole.LEADER)
        groupMembershipRepository.save(groupMembership)

        val count = groupMembershipRepository.countByGroupIdAndStatusAndDisabled(
            groupId, MembershipStatus.APPROVED, false
        )

        if (group.maxRecruitCount <= count)
            group.modifyRecruitStatus(RecruitStatus.CLOSED)

        return groupId
    }

    /**
     * 모임(Group) 단 건 조회
     *
     * @param groupId - 모임 ID
     * @return 모임 응답 DTO
     */
    fun getGroup(@Min(1) groupId: Long): GroupResponse.Detail = GroupResponse.toDetail(
        groupRepository.findByIdAndDisabled(groupId, false)
            .orElseThrow { GroupException(GroupErrorCode.GROUP_NOT_FOUND) }
    )

    /**
     * 모임(Group) 단 건 조회
     *
     * @param groupId  - 모임 ID
     * @param memberId - 회원 ID
     * @return 모임 응답 DTO
     */
    fun getGroup(@Min(1) groupId: Long, @Min(1) memberId: Long): GroupResponse.DetailWithLike {
        val opGroupMembership = groupMembershipRepository.findByGroupIdAndMemberIdAndDisabled(
            groupId, memberId, false
        )

        val isLiked = groupLikeService.isLiked(groupId, memberId)

        if (opGroupMembership.isPresent) {
            val groupMembership = opGroupMembership.get()
            return GroupResponse.toDetailWithLike(
                groupMembership.group,
                opGroupMembership.get().status == MembershipStatus.PENDING,
                opGroupMembership.get().status == MembershipStatus.APPROVED,
                opGroupMembership.get().status == MembershipStatus.APPROVED && opGroupMembership.get().groupRole == GroupRole.LEADER,
                isLiked
            )
        }

        return GroupResponse.toDetailWithLike(
            groupRepository.findByIdAndDisabled(groupId, false)
                .orElseThrow { GroupException(GroupErrorCode.GROUP_NOT_FOUND) },
            isLiked
        )
    }

    /**
     * 모임(Group) 다 건 조회
     *
     * @return 모임 응답 DTO 목록(List)
     */
    fun getGroups(memberId: Long) = groupRepository.findAllByDisabled(false)
        .map { group ->
            val isLiked = groupLikeService.isLiked(group.id!!, memberId) // isLiked 값 계산
            GroupResponse.toListInfoWithLike(group, isLiked) // isLiked 값을 전달
        }

    /**
     * 모임(Group) 다 건 조회
     *
     * @return 모임 응답 DTO 목록(Page)
     */
    fun getGroups(pageable: Pageable, memberId: Long) = groupRepository.findAllByDisabled(false, pageable)
        .map { group ->
            val isLiked = groupLikeService.isLiked(group.id!!, memberId) // isLiked 값 계산
            GroupResponse.toListInfoWithLike(group, isLiked) // isLiked 값을 전달
        }

    /**
     * 모임 이름으로 모임(Group) 다 건 조회
     *
     * @param name - 모임 이름
     * @return 모임 응답 DTO 목록(List)
     */
    fun getGroupsByNameContaining(name: String, memberId: Long) = groupRepository.findAllByNameContainingAndDisabled(name, false)
        .map { group ->
            val isLiked = groupLikeService.isLiked(group.id!!, memberId) // isLiked 값 계산
            GroupResponse.toListInfoWithLike(group, isLiked) // isLiked 값을 전달
        }

    /**
     * 모임 이름으로 모임(Group) 다 건 조회
     *
     * @param name     - 모임 이름
     * @param pageable - 페이징 객체
     * @return 모임 응답 DTO 목록(Page)
     */
    fun getGroupsByNameContaining(name: String, pageable: Pageable, memberId: Long) =
        groupRepository.findAllByNameContainingAndDisabled(name, false, pageable)
            .map { group ->
                val isLiked = groupLikeService.isLiked(group.id!!, memberId) // isLiked 값 계산
                GroupResponse.toListInfoWithLike(group, isLiked) // isLiked 값을 전달
            }

    /**
     * 상세 주소로 모임(Group) 다 건 조회
     *
     * @param province - 시/도
     * @param city     - 시/군/구
     * @param town     - 읍/면/동
     * @return 모임 응답 DTO 목록(List)
     */
    fun getGroupsByRegion(province: String, city: String, town: String, memberId: Long) =
        groupRepository.findAllByRegion(province, city, town, false)
            .map { group ->
                val isLiked = groupLikeService.isLiked(group.id!!, memberId) // isLiked 값 계산
                GroupResponse.toListInfoWithLike(group, isLiked) // isLiked 값을 전달
            }

    /**
     * 상세 주소로 모임(Group) 다 건 조회
     *
     * @param province - 시/도
     * @param city     - 시/군/구
     * @param town     - 읍/면/동
     * @param pageable - 페이징 객체
     * @return 모임 응답 DTO 목록(Page)
     */
    fun getGroupsByRegion(province: String, city: String, town: String, pageable: Pageable, memberId: Long) =
        groupRepository.findAllByRegion(province, city, town, false, pageable)
            .map { group ->
                val isLiked = groupLikeService.isLiked(group.id!!, memberId) // isLiked 값 계산
                GroupResponse.toListInfoWithLike(group, isLiked) // isLiked 값을 전달
            }

    /**
     * 모임 이름과 상세 주소로 모임(Group) 다 건 조회
     *
     * @param name     - 모임 이름
     * @param province - 시/도
     * @param city     - 시/군/구
     * @param town     - 읍/면/동
     * @return 모임 응답 DTO 목록(List)
     */
    fun getGroupsByNameContainingAndRegion(name: String, province: String, city: String, town: String, memberId: Long) =
        groupRepository.findAllByNameContainingAndRegion(name, province, city, town, false)
            .map { group ->
                val isLiked = groupLikeService.isLiked(group.id!!, memberId) // isLiked 값 계산
                GroupResponse.toListInfoWithLike(group, isLiked) // isLiked 값을 전달
            }

    /**
     * 모임 이름과 상세 주소로 모임(Group) 다 건 조회
     *
     * @param name     - 모임 이름
     * @param province - 시/도
     * @param city     - 시/군/구
     * @param town     - 읍/면/동
     * @param pageable - 페이징 객체
     * @return 모임 응답 DTO 목록(Page)
     */
    fun getGroupsByNameContainingAndRegion(
        name: String,
        province: String,
        city: String,
        town: String,
        pageable: Pageable,
        memberId: Long
    ) = groupRepository.findAllByNameContainingAndRegion(name, province, city, town, false, pageable)
        .map { group ->
            val isLiked = groupLikeService.isLiked(group.id!!, memberId) // isLiked 값 계산
            GroupResponse.toListInfoWithLike(group, isLiked) // isLiked 값을 전달
        }

    /**
     * 카테고리와 모임 이름, 상세 주소로 모임(Group) 다 건 조회
     *
     * @param dto - 모임 검색 요청 DTO
     * @return 모임 응답 DTO 목록(List)
     */
    fun getGroupsBySearch(dto: GroupRequest.Search, memberId: Long) =
        groupRepository.findAllByCategoryAndRecruitStatusAndNameContainingAndRegion(
            dto.categoryName,
            dto.recruitStatus,
            dto.name,
            dto.province,
            dto.city,
            dto.town,
            false
        ).map { group ->
            val isLiked = groupLikeService.isLiked(group.id!!, memberId) // isLiked 값 계산
            GroupResponse.toListInfoWithLike(group, isLiked) // isLiked 값을 전달
        }

    /**
     * 카테고리와 모임 이름, 상세 주소로 모임(Group) 다 건 조회
     *
     * @param dto      - 모임 검색 요청 DTO
     * @param pageable - 페이징 객체
     * @return 모임 응답 DTO 목록(Page)
     */
    fun getGroupsBySearch(dto: GroupRequest.Search, pageable: Pageable, memberId: Long) =
        groupRepository.findAllByCategoryAndRecruitStatusAndNameContainingAndRegion(
            dto.categoryName,
            dto.recruitStatus,
            dto.name,
            dto.province,
            dto.city,
            dto.town,
            false,
            pageable
        ).map { group ->
            val isLiked = groupLikeService.isLiked(group.id!!, memberId) // isLiked 값 계산
            val groupResponse = GroupResponse.toListInfoWithLike(group, isLiked)
            groupResponse // 반환
        }

    /**
     * 모임(Group) 수정
     *
     * @param groupId  - 모임 ID
     * @param memberId - 회원 ID
     * @param dto      - 모임(Group) 수정 요청 DTO
     * @return 모임 응답 DTO
     */
    @CustomLock(key = "'group:' + #groupId")
    fun modifyGroup(@Min(1) groupId: Long, @Min(1) memberId: Long, dto: GroupRequest.Update): GroupResponse.Detail {
        val groupMembership = groupMembershipRepository.findByGroupIdAndMemberIdAndDisabled(
            groupId,
            memberId,
            false
        ).orElseThrow { GroupMembershipException(GroupMembershipErrorCode.GROUP_MEMBERSHIP_NOT_FOUND) }

        //회원의 모임 내 권한 확인
        if (groupMembership.groupRole != GroupRole.LEADER || groupMembership.status != MembershipStatus.APPROVED)
            throw GroupMembershipException(GroupMembershipErrorCode.GROUP_MEMBERSHIP_NO_PERMISSION)

        val group = groupRepository.findByIdAndDisabled(groupId, false)
            .orElseThrow { GroupException(GroupErrorCode.GROUP_NOT_FOUND) }

        //수정할 카테고리 조회
        val newCategory = categoryRepository.findByNameAndDisabled(dto.categoryName, false)
            .orElseThrow { CategoryException(CategoryErrorCode.CATEGORY_NOT_FOUND) }

        if (
            dto.maxRecruitCount < groupMembershipRepository.countByGroupIdAndStatusAndDisabled(
                groupId,
                MembershipStatus.APPROVED,
                false
            )
        )
            throw GroupException(GroupErrorCode.GROUP_MAXIMUM_NUMBER_OF_MEMBERS)

        val newRecruitStatus = RecruitStatus.valueOf(dto.recruitStatus)
        if (newRecruitStatus == RecruitStatus.CLOSED) newRecruitStatus.modifyForceStatus(true)
        group.modifyName(dto.name)
            .modifyRegion(dto.province, dto.city, dto.town)
            .modifyDescription(dto.description)
            .modifyRecruitStatus(newRecruitStatus)
            .modifyMaxRecruitCount(dto.maxRecruitCount)
            .modifyCategory(newCategory)

        return GroupResponse.toDetail(group)
    }

    /**
     * 모임(Group) 삭제(Soft Delete)
     *
     * @param groupId  - 모임 ID
     * @param memberId - 회원 ID
     * @return 모임 비활성화(disabled) 여부
     */
    @CustomLock(key = "'group:' + #groupId")
    @Transactional
    fun deleteGroup(@Min(1) groupId: Long, @Min(1) memberId: Long): Boolean {
        val groupMembership = groupMembershipRepository.findByGroupIdAndMemberIdAndDisabled(
            groupId,
            memberId,
            false
        ).orElseThrow { GroupMembershipException(GroupMembershipErrorCode.GROUP_MEMBERSHIP_NOT_FOUND) }

        //회원의 모임 내 권한 확인
        if (groupMembership.groupRole != GroupRole.LEADER || groupMembership.status != MembershipStatus.APPROVED)
            throw GroupMembershipException(GroupMembershipErrorCode.GROUP_MEMBERSHIP_NO_PERMISSION)

        val group = groupRepository.findByIdAndDisabled(groupId, false)
            .orElseThrow { GroupException(GroupErrorCode.GROUP_NOT_FOUND) }

        groupLikeRepository.deleteByGroupId(groupId)

        group.deactivate()
        groupMembershipRepository.updateDisabledForAllGroupMembership(
            groupId,
            true
        )    //해당 모임 ID를 갖는 멤버십 일괄 삭제(Soft Delete)

        entityManager.flush()
        entityManager.clear()   //벌크 연산 후 영속성 컨텍스트 초기화

        return group.disabled
    }
}
