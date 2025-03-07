package com.app.backend.domain.post.service.post.domain.chat.message.controller;

import com.app.backend.domain.category.entity.Category;
import com.app.backend.domain.chat.message.entity.Message;
import com.app.backend.domain.chat.message.repository.MessageRepository;
import com.app.backend.domain.chat.room.entity.ChatRoom;
import com.app.backend.domain.group.entity.Group;
import com.app.backend.domain.group.entity.GroupMembership;
import com.app.backend.domain.group.entity.GroupRole;
import com.app.backend.domain.group.entity.MembershipStatus;
import com.app.backend.domain.member.entity.Member;
import com.app.backend.domain.member.entity.MemberDetails;
import com.app.backend.domain.post.service.post.domain.chat.util.TestDataUtil;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
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
class MessageControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private MessageRepository messageRepository;

	@Autowired
	private TestDataUtil testDataUtil;

	private Long chatRoomId;

	private Member savedMember;

	@BeforeEach
	void setUp() {
		// DB에 테스트용 데이터 준비 (테스트용 채팅방 및 메시지 데이터 삽입)

		// 멤버 생성 & 저장
		savedMember = testDataUtil.createAndSaveMember("testUser", "testNickname");
		Member savedMember2 = testDataUtil.createAndSaveMember("testUser2", "testNickname2");

		// 카테고리 생성 & 저장
		Category category = testDataUtil.createAndSaveCategory("테스트 카테고리");

		// 그룹 생성 & 저장
		testDataUtil.createAndSaveGroup(
			"대구fc 팬 모임1",
			"대구 광역시",
			"북구",
			"고성로 191",
			"대팍 직관 같이가실분",
			category
		);
		Group group2 = testDataUtil.createAndSaveGroup(
			"대구fc 팬 모임1",
			"대구 광역시",
			"북구",
			"고성로 191",
			"대팍 직관 같이가실분",
			category
		);

		// 그룹과 멤버십 관계 설정
		testDataUtil.createAndSaveGroupMembership(savedMember, group2, GroupRole.LEADER);
		GroupMembership groupMembership2 = testDataUtil.createAndSaveGroupMembership(savedMember2, group2, GroupRole.PARTICIPANT);
		testDataUtil.saveGroupMembership(groupMembership2.modifyStatus(MembershipStatus.APPROVED));

		// 채팅방 생성 및 그룹에 연결
		ChatRoom chatRoom = testDataUtil.createAndSaveChatRoom(group2);
		group2.setChatRoom(chatRoom);
		chatRoomId = chatRoom.getId();

		// 채팅방에 메시지 저장
		// 채팅 메시지를 반복문으로 생성
		for (int i = 0; i < 30; i++) { // 10개의 메시지 생성 예시
			String messageContent = "메시지 " + (i + 1); // 메시지 내용
			Long senderId = (i % 2 == 0) ? savedMember.getId() : savedMember2.getId(); // 번갈아가며 보낸 사람 설정
			String senderNickname = (i % 2 == 0) ? savedMember.getNickname() : savedMember2.getNickname(); // 닉네임 설정

			createAndSaveMessage(chatRoom.getId(), senderId, senderNickname, messageContent);
		}
	}

	@AfterEach
	void tearDown() {
		// 테스트 후 데이터 삭제
		deleteAllMessages();
	}

	@Test
	@DisplayName("[성공] 채팅 메세지 조회")
	void getMessagesByChatRoomId() throws Exception {
		// given
		int page = 0; // 첫 번째 페이지
		int size = 20; // 한 페이지에 표시할 메시지 수

		// when
		ResultActions resultActions = mockMvc.perform(get("/api/v1/chatrooms/{chatRoomId}/messages", chatRoomId)
			.param("page", String.valueOf(page))
			.param("size", String.valueOf(size))
			.with(user(new MemberDetails(savedMember))));

		// then
		resultActions.andExpect(status().isOk())
			.andExpect(jsonPath("$.isSuccess").value(true))
			.andExpect(jsonPath("$.code").value("200"))
			.andExpect(jsonPath("$.message").value("메세지 조회 성공"))
			.andExpect(jsonPath("$.data.content.length()").value(20));
	}

	public void createAndSaveMessage(Long chatRoomId, Long senderId, String senderNickname, String content) {
		Message message = Message.builder()
			.chatRoomId(chatRoomId)
			.senderId(senderId)
			.senderNickname(senderNickname)
			.content(content)
			.disabled(false)
			.build();
		messageRepository.save(message);
	}

	public void deleteAllMessages() {
		messageRepository.deleteAll();
	}
}