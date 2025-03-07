package com.app.backend.domain.chat.util

import com.app.backend.domain.category.entity.Category
import com.app.backend.domain.category.repository.CategoryRepository
import com.app.backend.domain.chat.room.entity.ChatRoom
import com.app.backend.domain.chat.room.repository.ChatRoomRepository
import com.app.backend.domain.group.entity.*
import com.app.backend.domain.group.repository.*
import com.app.backend.domain.member.entity.Member
import com.app.backend.domain.member.repository.MemberRepository
import org.springframework.boot.test.context.TestComponent
import jakarta.transaction.Transactional

@TestComponent
@Transactional
class TestDataUtil(
	private val memberRepository: MemberRepository,
	private val groupRepository: GroupRepository,
	private val groupMembershipRepository: GroupMembershipRepository,
	private val chatRoomRepository: ChatRoomRepository,
	private val categoryRepository: CategoryRepository
) {

	fun createAndSaveMember(username: String, nickname: String): Member {
		return memberRepository.save(Member.create(username, null, nickname, "USER", false, null, null))
	}

	fun createAndSaveCategory(categoryName: String): Category {
		return categoryRepository.save(Category(categoryName))
	}

	fun createAndSaveGroup(
		name: String, province: String, city: String, town: String,
		description: String, category: Category
	): Group {
		return groupRepository.save(
			Group.of(name, province, city, town, description, RecruitStatus.RECRUITING, 10, category)
		)
	}

	fun createAndSaveGroupMembership(member: Member, group: Group, groupRole: GroupRole): GroupMembership {
		return groupMembershipRepository.save(GroupMembership.of(member, group, groupRole))
	}

	fun saveGroupMembership(groupMembership: GroupMembership) {
		groupMembershipRepository.save(groupMembership)
	}

	fun createAndSaveChatRoom(group: Group): ChatRoom {
		return chatRoomRepository.save(ChatRoom(group))
	}
}