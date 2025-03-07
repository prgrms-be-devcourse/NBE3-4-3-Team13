package com.app.backend.domain.post.service.post.domain.chat.room.controller;

import com.app.backend.domain.category.entity.Category;
import com.app.backend.domain.chat.room.entity.ChatRoom;
import com.app.backend.domain.group.entity.Group;
import com.app.backend.domain.group.entity.GroupMembership;
import com.app.backend.domain.group.entity.GroupRole;
import com.app.backend.domain.group.entity.MembershipStatus;
import com.app.backend.domain.member.entity.Member;
import com.app.backend.domain.member.entity.MemberDetails;
import com.app.backend.domain.post.service.post.domain.chat.util.TestDataUtil;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
@Transactional
class ChatRoomControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private TestDataUtil testDataUtil;

	@Test
	@DisplayName("[성공] 채팅방 상세 정보 조회")
	void getChatRoom() throws Exception {
		// given

		// 멤버 생성 & 저장
		Member savedMember = testDataUtil.createAndSaveMember("testUser", "testNickname");
		Member savedMember2 = testDataUtil.createAndSaveMember("testUser2", "testNickname2");

		// 카테고리 생성 & 저장
		Category category = testDataUtil.createAndSaveCategory("테스트 카테고리");

		// 그룹 생성 & 저장
		Group group1 = testDataUtil.createAndSaveGroup(
			"대구fc 팬 모임1",
			"대구 광역시",
			"북구",
			"고성로 191",
			"대팍 직관 같이가실분",
			category
		);

		// 그룹과 멤버십 관계 설정
		GroupMembership groupMembership1 = testDataUtil.createAndSaveGroupMembership(savedMember, group1, GroupRole.LEADER);
		GroupMembership groupMembership2 = testDataUtil.createAndSaveGroupMembership(savedMember2, group1, GroupRole.PARTICIPANT);
		testDataUtil.saveGroupMembership(groupMembership2.modifyStatus(MembershipStatus.APPROVED));

		// 채팅방 생성 및 그룹에 연결
		ChatRoom chatRoom = testDataUtil.createAndSaveChatRoom(group1);
		group1.setChatRoom(chatRoom);

		// when
		ResultActions resultActions = mockMvc.perform(get("/api/v1/chatrooms/{chatRoomId}", chatRoom.getId())
			.with(user(new MemberDetails(savedMember))));

		// then
		resultActions.andExpect(status().isOk())
			.andExpect(jsonPath("$.isSuccess").value(true))
			.andExpect(jsonPath("$.code").value("200"))
			.andExpect(jsonPath("$.message").value("채팅방 상세 조회 성공"))
			.andExpect(jsonPath("$.data.chatRoomId").value(chatRoom.getId()))
			.andExpect(jsonPath("$.data.group.groupId").value(group1.getId()))
			.andExpect(jsonPath("$.data.group.groupName").value("대구fc 팬 모임1"))
			.andExpect(jsonPath("$.data.group.participantCount").value(2))
			.andExpect(jsonPath("$.data.members[0].memberId").value(savedMember.getId()))
			.andExpect(jsonPath("$.data.members[0].memberNickname").value(savedMember.getNickname()))
			.andExpect(jsonPath("$.data.members[0].groupRole").value(groupMembership1.getGroupRole().name()))
			.andExpect(jsonPath("$.data.members[1].memberId").value(savedMember2.getId()))
			.andExpect(jsonPath("$.data.members[1].memberNickname").value(savedMember2.getNickname()))
			.andExpect(jsonPath("$.data.members[1].groupRole").value(groupMembership2.getGroupRole().name()));
	}

	@Test
	@DisplayName("[실패] 채팅방 상세 조회 - 채팅방을 찾을 수 없음")
	void getChatRoomNotFound() throws Exception {
		// given

		// 멤버 생성 & 저장
		Member savedMember = testDataUtil.createAndSaveMember("testUser", "testNickname");

		// when
		ResultActions resultActions = mockMvc.perform(get("/api/v1/chatrooms/1")
			.with(user(new MemberDetails(savedMember))));

		// then
		resultActions.andExpect(status().isNotFound())
			.andExpect(jsonPath("$.isSuccess").value(false))
			.andExpect(jsonPath("$.code").value("CH001"));
	}
}