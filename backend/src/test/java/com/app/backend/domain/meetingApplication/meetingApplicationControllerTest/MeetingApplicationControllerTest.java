package com.app.backend.domain.meetingApplication.meetingApplicationControllerTest;

import com.app.backend.domain.category.entity.Category;
import com.app.backend.domain.category.repository.CategoryRepository;
import com.app.backend.domain.group.entity.Group;
import com.app.backend.domain.group.entity.GroupMembership;
import com.app.backend.domain.group.entity.GroupRole;
import com.app.backend.domain.group.entity.RecruitStatus;
import com.app.backend.domain.group.repository.GroupMembershipRepository;
import com.app.backend.domain.group.repository.GroupRepository;
import com.app.backend.domain.meetingApplication.dto.MeetingApplicationReqBody;
import com.app.backend.domain.meetingApplication.entity.MeetingApplication;
import com.app.backend.domain.meetingApplication.exception.MeetingApplicationErrorCode;
import com.app.backend.domain.meetingApplication.exception.MeetingApplicationException;
import com.app.backend.domain.meetingApplication.repository.MeetingApplicationRepository;
import com.app.backend.domain.meetingApplication.service.MeetingApplicationService;
import com.app.backend.domain.member.entity.Member;
import com.app.backend.domain.member.entity.MemberDetails;
import com.app.backend.domain.member.repository.MemberRepository;
import com.app.backend.global.annotation.CustomWithMockUser;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import static org.mockito.BDDMockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
public class MeetingApplicationControllerTest {

	@Autowired
	private ObjectMapper objectMapper;

	@Autowired
	private GroupRepository groupRepository;

	@Autowired
	private MemberRepository memberRepository;

	@Autowired
	private GroupMembershipRepository groupMembershipRepository;

	@Autowired
	private MeetingApplicationRepository meetingApplicationRepository;

	@Autowired
	private CategoryRepository categoryRepository;

	@MockitoBean
	private MeetingApplicationService meetingApplicationService;

	@Autowired
	MockMvc mvc;

	private Group group;
	private Member member;
	private Category category;

	@BeforeEach
	void setup() {
		// repository 초기화
		meetingApplicationRepository.deleteAll();
		groupMembershipRepository.deleteAll();
		groupRepository.deleteAll();
		categoryRepository.deleteAll();
		memberRepository.deleteAll();

		category = categoryRepository.save(new Category("category"));

		group = Group.Companion.of(
				"test group",
				"test province",
				"test city",
				"test town",
				"test description",
				RecruitStatus.RECRUITING,
				10,
				category
		);

		member = memberRepository.save(
				Member.create(
						"testUser",
						"password123",
						"testUser",
						"USER",
						false,
						Member.Provider.LOCAL,
						null
				)
		);

		groupRepository.save(group);
		memberRepository.save(member);
	}

	@Test
	@DisplayName("신청 폼 작성")
	@CustomWithMockUser
	void t1() throws Exception {
		MemberDetails mockUser = new MemberDetails(member);

		MeetingApplicationReqBody request = new MeetingApplicationReqBody("신청합니다.");

		MeetingApplication mockMeetingApplication = new MeetingApplication(group, member, request.getContext());

		given(meetingApplicationService.create(group.getId(), request, member.getId()))
			.willReturn(mockMeetingApplication);

		// When
		ResultActions resultActions = mvc.perform(MockMvcRequestBuilders.post("/api/v1/groups/{groupId}", group.getId())
			.with(user(mockUser))  // MockUser를 사용하여 로그인된 상태 시뮬레이션
			.contentType(MediaType.APPLICATION_JSON)
			.content(objectMapper.writeValueAsString(request)) // 요청 본문
		);

		// Then
		resultActions.andExpect(status().isCreated());
		resultActions.andExpect(jsonPath("$.code").value(HttpStatus.CREATED.value()));
		resultActions.andExpect(jsonPath("$.message")
			.value(group.getId() + "번 모임에 성공적으로 가입 신청을 하셨습니다."));
		resultActions.andExpect(jsonPath("$.data.context").value("신청합니다."));
		resultActions.andDo(print());

		verify(meetingApplicationService, times(1)).create(group.getId(), request, member.getId());

	}

	@Test
	@DisplayName("신청 폼 제출 - 그룹 정원 초과 시 예외 처리")
	@CustomWithMockUser
	void t2() throws Exception {
		// Given: 그룹의 정원을 1명으로 설정
		Group oneMemberGroup = Group.Companion.of(
				"test group",
				"test province",
				"test city",
				"test town",
				"test description",
				RecruitStatus.RECRUITING,
				1,
				category
		);
		groupRepository.save(oneMemberGroup);  // 그룹 저장

		// 그룹에 첫 번째 멤버를 추가
		groupMembershipRepository.save(GroupMembership.Companion.of(member, oneMemberGroup, GroupRole.LEADER));

		// 새로운 회원을 만들고 저장
		Member newMember = Member.create(
				"testUser",
				"password123",
				"testUser",
				"USER",
				false,
				Member.Provider.LOCAL,
				null
		);
		memberRepository.save(newMember);

		MeetingApplicationReqBody request = new MeetingApplicationReqBody("Test Application");

		// 정원 초과로 예외 발생
		given(meetingApplicationService.create(oneMemberGroup.getId(), request, newMember.getId()))
			.willThrow(new MeetingApplicationException(MeetingApplicationErrorCode.GROUP_MEMBER_LIMIT_EXCEEDED));

		MemberDetails mockUser = new MemberDetails(newMember);

		// When & Then
		mvc.perform(MockMvcRequestBuilders.post("/api/v1/groups/{groupId}", oneMemberGroup.getId())
				.with(user(mockUser))
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request))
				.header("Authorization", "Bearer testToken"))
			.andExpect(status().isConflict())  // 상태 코드 409 Conflict
			.andExpect(result -> {
				String responseContent = result.getResponse().getContentAsString();
				assert(responseContent.contains("그룹 정원이 초과되었습니다."));
			});
	}

	@Test
	@DisplayName("meeting application 조회")
	@CustomWithMockUser
	void t3() throws Exception {
		// Given
		Member leader = memberRepository.save(Member.create(
				"testUser",
				"password123",
				"testUser",
				"USER",
				false,
				Member.Provider.LOCAL,
				null
		));

		MemberDetails mockUser = new MemberDetails(leader);

		GroupMembership groupMembership = groupMembershipRepository.save(GroupMembership.Companion.of(leader, group, GroupRole.LEADER));

		meetingApplicationRepository.save(new MeetingApplication(group, member, "Test Application"));

		// When & Then
		mvc.perform(get("/api/v1/groups/{groupId}/meeting_applications", group.getId())
				.with(user(mockUser))  // 인증된 사용자 설정
				.contentType(MediaType.APPLICATION_JSON))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.message").value("meeting application 조회 성공"));
	}

	@Test
	@DisplayName("meeting application 상세 조회")
	@CustomWithMockUser
	void t4() throws Exception {
		// Given
		Member leader = memberRepository.save(Member.create(
				"testUser",
				"password123",
				"testUser",
				"USER",
				false,
				Member.Provider.LOCAL,
				null
		));

		MemberDetails mockUser = new MemberDetails(leader);

		GroupMembership groupMembership = groupMembershipRepository.save(GroupMembership.Companion.of(leader, group, GroupRole.LEADER));

		meetingApplicationRepository.save(new MeetingApplication(group, member, "Test Application"));

		// When & Then
		mvc.perform(get("/api/v1/groups/{groupId}/meeting_applications/{meetingApplicationId}", group.getId(), 1)
				.with(user(mockUser))  // 인증된 사용자 설정
				.contentType(MediaType.APPLICATION_JSON))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.message").value("meeting application 상세 조회 성공"));

	}

}
