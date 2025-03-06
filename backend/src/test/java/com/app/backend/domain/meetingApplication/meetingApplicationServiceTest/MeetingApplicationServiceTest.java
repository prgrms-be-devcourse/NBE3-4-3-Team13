package com.app.backend.domain.meetingApplication.meetingApplicationServiceTest;

import com.app.backend.domain.category.entity.Category;
import com.app.backend.domain.category.repository.CategoryRepository;
import com.app.backend.domain.group.entity.Group;
import com.app.backend.domain.group.entity.GroupMembership;
import com.app.backend.domain.group.entity.GroupRole;
import com.app.backend.domain.group.entity.MembershipStatus;
import com.app.backend.domain.group.repository.GroupMembershipRepository;
import com.app.backend.domain.group.repository.GroupRepository;
import com.app.backend.domain.meetingApplication.dto.MeetingApplicationReqBody;
import com.app.backend.domain.meetingApplication.exception.MeetingApplicationErrorCode;
import com.app.backend.domain.meetingApplication.exception.MeetingApplicationException;
import com.app.backend.domain.meetingApplication.repository.MeetingApplicationRepository;
import com.app.backend.domain.meetingApplication.service.MeetingApplicationService;
import com.app.backend.domain.member.entity.Member;
import com.app.backend.domain.member.entity.Member.Provider;
import com.app.backend.domain.member.repository.MemberRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

;

@SpringBootTest
class MeetingApplicationServiceTest {

	@Autowired
	private MeetingApplicationService meetingApplicationService;

	@Autowired
	private MeetingApplicationRepository meetingApplicationRepository;

	@Autowired
	private GroupRepository groupRepository;

	@Autowired
	private GroupMembershipRepository groupMembershipRepository;

	@Autowired
	private MemberRepository memberRepository;

	@Autowired
	private CategoryRepository categoryRepository;

	private Group group;
	private Member member;
	private Category category;

	@BeforeEach
	void setUp() {
		// repository 초기화
		meetingApplicationRepository.deleteAll();
		groupMembershipRepository.deleteAll();
		groupRepository.deleteAll();
		categoryRepository.deleteAll();
		memberRepository.deleteAll();

		// 카테고리 생성
		category = categoryRepository.save(new Category("category"));

		// 그룹 생성
		group = groupRepository.save(
				Group.of(
						"test group",
						"test province",
						"test city",
						"test town",
						"test description",
						10,
						category
				)
		);

		// 멤버 생성
		member = memberRepository.save(
				Member.create(
						"testUser",
						"password123",
						"testUser",
						"USER",
						false,
						Provider.LOCAL,
						null
				)
		);
	}

	@Test
	@DisplayName("Fail : 그룹 정원 초과 시 예외 처리")
	void t1() {
		Group limitedGroup = groupRepository.save(
				Group.of(
						"test limited group",
						"test province",
						"test city",
						"test town",
						"test description",
						1,
						category
				)
		);

		// 그룹 정원 초과를 테스트할 두 명의 멤버 생성
		Member member1 = memberRepository.save(
				Member.create(
						"testUser1",
						"password123",
						"nickname1",
						"USER",
						false,
						Provider.LOCAL,
						null
				)
		);

		Member member2 = memberRepository.save(
				Member.create(
						"testUser2",
						"password123",
						"nickname2",
						"USER",
						false,
						Provider.LOCAL,
						null
				)
		);

		groupMembershipRepository.save(
				GroupMembership.of(
						member1,
						limitedGroup,
						GroupRole.LEADER
				)
		);

		MeetingApplicationReqBody request = new MeetingApplicationReqBody("Test Application");

		assertThatThrownBy(() -> {
			meetingApplicationService.validateGroupMemberLimit(limitedGroup.getId());
			meetingApplicationService.create(limitedGroup.getId(), request, member2.getId());
		})
				.isInstanceOf(MeetingApplicationException.class)
				.hasFieldOrPropertyWithValue("domainErrorCode", MeetingApplicationErrorCode.GROUP_MEMBER_LIMIT_EXCEEDED)
				.hasMessage("그룹 정원이 초과되었습니다.");
	}

	@Test
	@DisplayName("Success : 기존 그룹 멤버십이 REJECTED 상태인 회원이 신청하면 PENDING으로 변경되고 MeetingApplication이 저장")
	void t2() {
		GroupMembership rejectedMembership = groupMembershipRepository.save(
				GroupMembership.of(
						member, // 기존 멤버
						group, // 그룹
						GroupRole.PARTICIPANT // 참여자 역할
				)
		);


		rejectedMembership.modifyStatus(MembershipStatus.REJECTED);
		groupMembershipRepository.save(rejectedMembership);

		MeetingApplicationReqBody request = new MeetingApplicationReqBody("Test Application");

		meetingApplicationService.create(group.getId(), request, member.getId());

		GroupMembership updatedMembership = groupMembershipRepository
				.findByGroupIdAndMemberIdAndDisabled(group.getId(), member.getId(), false)
				.orElseThrow();

		assertThat(updatedMembership.getStatus()).isEqualTo(MembershipStatus.PENDING);
	}

	@Test
	@DisplayName("Success : 기존 그룹 멤버십이 LEAVE 상태인 회원이 신청하면 PENDING으로 변경되고 MeetingApplication이 저장")
	void t3() {
		// Given
		GroupMembership leaveMembership = groupMembershipRepository.save(
				GroupMembership.of(
						member,
						group,
						GroupRole.PARTICIPANT
				)
		);


		leaveMembership.modifyStatus(MembershipStatus.LEAVE);
		groupMembershipRepository.save(leaveMembership);

		MeetingApplicationReqBody request = new MeetingApplicationReqBody("Test Application");

		// When
		meetingApplicationService.create(group.getId(), request, member.getId());

		// Then
		GroupMembership updatedMembership = groupMembershipRepository
				.findByGroupIdAndMemberIdAndDisabled(group.getId(), member.getId(), false)
				.orElseThrow();

		assertThat(updatedMembership.getStatus()).isEqualTo(MembershipStatus.PENDING);

		// MeetingApplication이 저장되었는지 확인
		var applications = meetingApplicationRepository.findByGroupIdAndDisabled(group.getId(), false);
		assertThat(applications).isNotNull();
		assertThat(applications.size()).isEqualTo(1);

		// 저장된 MeetingApplication이 올바른 값인지 확인
		var application = applications.get(0);
		assertThat(application.getMember().getId()).isEqualTo(member.getId());
		assertThat(application.getGroup().getId()).isEqualTo(group.getId());
		assertThat(application.getContext()).isEqualTo(request.getContext());
	}
}
