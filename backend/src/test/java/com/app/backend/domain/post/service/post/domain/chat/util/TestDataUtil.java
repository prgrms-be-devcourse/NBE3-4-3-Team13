package com.app.backend.domain.post.service.post.domain.chat.util;

import com.app.backend.domain.category.entity.Category;
import com.app.backend.domain.category.repository.CategoryRepository;
import com.app.backend.domain.chat.room.entity.ChatRoom;
import com.app.backend.domain.chat.room.repository.ChatRoomRepository;
import com.app.backend.domain.group.entity.Group;
import com.app.backend.domain.group.entity.GroupMembership;
import com.app.backend.domain.group.entity.GroupRole;
import com.app.backend.domain.group.entity.RecruitStatus;
import com.app.backend.domain.group.repository.GroupMembershipRepository;
import com.app.backend.domain.group.repository.GroupRepository;
import com.app.backend.domain.member.entity.Member;
import com.app.backend.domain.member.repository.MemberRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@Transactional
@RequiredArgsConstructor
public class TestDataUtil {

	private final MemberRepository memberRepository;
	private final GroupRepository groupRepository;
	private final GroupMembershipRepository groupMembershipRepository;
	private final ChatRoomRepository chatRoomRepository;
	private final CategoryRepository categoryRepository;

	public Member createAndSaveMember(String username, String nickname) {
		Member member = Member.builder()
			.username(username)
			.nickname(nickname)
			.role("USER")
			.build();
		return memberRepository.save(member);
	}

	public Category createAndSaveCategory(String categoryName) {
		Category category = Category.builder()
			.name(categoryName)
			.build();
		return categoryRepository.save(category);
	}

	public Group createAndSaveGroup(String name, String province, String city, String town, String description,
		Category category) {
		Group group = Group.builder()
			.name(name)
			.province(province)
			.city(city)
			.town(town)
			.description(description)
			.recruitStatus(RecruitStatus.RECRUITING)
			.maxRecruitCount(10)
			.category(category)
			.build();
		return groupRepository.save(group);
	}

	public GroupMembership createAndSaveGroupMembership(Member member, Group group, GroupRole groupRole) {
		GroupMembership groupMembership = GroupMembership.builder()
			.member(member)
			.group(group)
			.groupRole(groupRole)
			.build();
		groupMembershipRepository.save(groupMembership);
		return groupMembership;
	}

	public void saveGroupMembership(GroupMembership groupMembership) {
		groupMembershipRepository.save(groupMembership);
	}

	public ChatRoom createAndSaveChatRoom(Group group) {
		ChatRoom chatRoom = ChatRoom.builder().group(group).build();
		return chatRoomRepository.save(chatRoom);
	}
}
