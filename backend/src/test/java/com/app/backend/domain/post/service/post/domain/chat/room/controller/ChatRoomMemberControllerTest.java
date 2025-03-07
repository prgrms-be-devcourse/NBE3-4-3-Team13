package com.app.backend.domain.post.service.post.domain.chat.room.controller;

import com.app.backend.domain.category.entity.Category;
import com.app.backend.domain.chat.room.entity.ChatRoom;
import com.app.backend.domain.group.entity.Group;
import com.app.backend.domain.group.entity.GroupRole;
import com.app.backend.domain.member.entity.Member;
import com.app.backend.domain.member.entity.MemberDetails;
import com.app.backend.domain.post.service.post.domain.chat.util.TestDataUtil;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
@Transactional
class ChatRoomMemberControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private TestDataUtil testDataUtil;

	@Test
	@DisplayName("[성공] 채팅방 목록 조회")
	void getChatRoomsByMemberId() throws Exception {
		// given

		// 멤버 생성 & 저장
		Member savedMember = testDataUtil.createAndSaveMember("testUser", "testNickname");

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
		Group group2 = testDataUtil.createAndSaveGroup(
			"대구fc 팬 모임2",
			"대구 광역시",
			"북구",
			"고성로 191",
			"대팍 직관 같이가실분2",
			category
		);

		// 그룹과 멤버십 관계 설정
		testDataUtil.createAndSaveGroupMembership(savedMember, group1, GroupRole.LEADER);
		testDataUtil.createAndSaveGroupMembership(savedMember, group2, GroupRole.LEADER);

		// 채팅방 생성 및 그룹에 연결
		ChatRoom chatRoom1 = testDataUtil.createAndSaveChatRoom(group1);
		ChatRoom chatRoom2 = testDataUtil.createAndSaveChatRoom(group2);

		// when
		ResultActions resultActions = mockMvc.perform(get("/api/v1/members/chatrooms")
			.with(user(new MemberDetails(savedMember))));

		// then
		resultActions.andExpect(status().isOk())
			.andExpect(jsonPath("$.isSuccess").value(true))
			.andExpect(jsonPath("$.code").value("200"))
			.andExpect(jsonPath("$.message").value("채팅방 목록 조회 성공"))
			.andExpect(jsonPath("$.data").isArray())
			.andExpect(jsonPath("$.data.length()").value(2)) // 두 개의 채팅방
			.andExpect(jsonPath("$.data[0].chatRoomId").value(chatRoom1.getId()))
			.andExpect(jsonPath("$.data[0].groupId").value(group1.getId()))
			.andExpect(jsonPath("$.data[0].groupName").value("대구fc 팬 모임1"))
			.andExpect(jsonPath("$.data[1].chatRoomId").value(chatRoom2.getId()))
			.andExpect(jsonPath("$.data[1].groupId").value(group2.getId()))
			.andExpect(jsonPath("$.data[1].groupName").value("대구fc 팬 모임2"));
	}
}