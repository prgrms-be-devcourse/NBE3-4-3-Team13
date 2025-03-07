package com.app.backend.domain.post.service.post.domain.meetingApplication.meetingApplicationServiceTest;

import com.app.backend.domain.category.entity.Category;
import com.app.backend.domain.category.repository.CategoryRepository;
import com.app.backend.domain.group.entity.*;
import com.app.backend.domain.group.repository.GroupMembershipRepository;
import com.app.backend.domain.group.repository.GroupRepository;
import com.app.backend.domain.meetingApplication.dto.MeetingApplicationReqBody;
import com.app.backend.domain.meetingApplication.entity.MeetingApplication;
import com.app.backend.domain.meetingApplication.exception.MeetingApplicationErrorCode;
import com.app.backend.domain.meetingApplication.exception.MeetingApplicationException;
import com.app.backend.domain.meetingApplication.repository.MeetingApplicationRepository;
import com.app.backend.domain.meetingApplication.service.MeetingApplicationService;
import com.app.backend.domain.member.entity.Member;
import com.app.backend.domain.member.repository.MemberRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

@SpringBootTest
public class MeetingApplicationServiceTest {

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

		category = categoryRepository.save(Category.builder()
			.name("category")
			.build());

		group = groupRepository.save(Group.builder()
			.name("test group")
			.province("test province")
			.city("test city")
			.town("test town")
			.description("test description")
			.recruitStatus(RecruitStatus.RECRUITING)
			.maxRecruitCount(10)
			.category(category)
			.build());

		member = memberRepository.save(Member.builder()
			.username("testUser")
			.nickname("nickname")
			.role("USER")
			.disabled(false)
			.build());
	}

	@Test
	@DisplayName("Fail : 그룹 정원 초과 시 예외 처리")
	void t1() {
		Group limitedGroup = groupRepository.save(Group.builder()
			.name("test limited group")
			.province("test province")
			.city("test city")
			.town("test town")
			.description("test description")
			.recruitStatus(RecruitStatus.RECRUITING)
			.maxRecruitCount(1)
			.category(category)
			.build());

		Member member1 = memberRepository.save(Member.builder()
			.username("testUser1")
			.nickname("nickname1")
			.role("USER")
			.disabled(false)
			.build());

		Member member2 = memberRepository.save(Member.builder()
			.username("testUser2")
			.nickname("nickname2")
			.role("USER")
			.disabled(false)
			.build());

		groupMembershipRepository.save(GroupMembership.builder()
			.group(limitedGroup)
			.member(member1)
			.groupRole(GroupRole.LEADER)
			.build());

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
		GroupMembership rejectedMembership = groupMembershipRepository.save(GroupMembership.builder()
			.group(group)
			.member(member)
			.groupRole(GroupRole.PARTICIPANT)
			.build());

		rejectedMembership.modifyStatus(MembershipStatus.REJECTED);
		groupMembershipRepository.save(rejectedMembership);

		MeetingApplicationReqBody request = new MeetingApplicationReqBody("Test Application");
		MeetingApplication savedApplication = meetingApplicationService.create(group.getId(), request, member.getId());

		GroupMembership updatedMembership = groupMembershipRepository.findByGroupIdAndMemberIdAndDisabled(group.getId(), member.getId(), false)
			.orElseThrow();
		assertThat(updatedMembership.getStatus()).isEqualTo(MembershipStatus.PENDING);
	}

	@Test
	@DisplayName("Success : 기존 그룹 멤버십이 LEAVE 상태인 회원이 신청하면 PENDING으로 변경되고 MeetingApplication이 저장")
	void t3() {
		// Given
		GroupMembership leaveMembership = groupMembershipRepository.save(GroupMembership.builder()
			.group(group)
			.member(member)
			.groupRole(GroupRole.PARTICIPANT)
			.build());

		leaveMembership.modifyStatus(MembershipStatus.LEAVE);
		groupMembershipRepository.save(leaveMembership);

		MeetingApplicationReqBody request = new MeetingApplicationReqBody("Test Application");

		// When
		MeetingApplication savedApplication = meetingApplicationService.create(group.getId(), request, member.getId());

		// Then
		// 기존 GroupMembership의 상태가 PENDING으로 변경되었는지 확인
		GroupMembership updatedMembership = groupMembershipRepository.findByGroupIdAndMemberIdAndDisabled(group.getId(), member.getId(), false)
			.orElseThrow();
		assertThat(updatedMembership.getStatus()).isEqualTo(MembershipStatus.PENDING);

		// MeetingApplication이 저장되었는지 확인
		List<MeetingApplication> applications = meetingApplicationRepository.findByGroupIdAndDisabled(group.getId(), false);
		assertThat(applications).isNotNull();
		assertThat(applications.size()).isEqualTo(1);

		// 저장된 MeetingApplication이 올바른 값인지 확인
		MeetingApplication application = applications.get(0);
		assertThat(application.getMember().getId()).isEqualTo(member.getId());
		assertThat(application.getGroup().getId()).isEqualTo(group.getId());
		assertThat(application.getContext()).isEqualTo(request.context());
	}


	@Test
	@DisplayName("Success : 기존 그룹 멤버십이 REJECTED 상태인 회원이 신청하면 PENDING으로 변경되고 MeetingApplication이 저장")
	void t4() {
		GroupMembership rejectedMembership = groupMembershipRepository.save(GroupMembership.builder()
			.group(group)
			.member(member)
			.groupRole(GroupRole.PARTICIPANT)
			.build());

		rejectedMembership.modifyStatus(MembershipStatus.REJECTED);
		groupMembershipRepository.save(rejectedMembership);

		MeetingApplicationReqBody request = new MeetingApplicationReqBody("Test Application");

		// When
		MeetingApplication savedApplication = meetingApplicationService.create(group.getId(), request, member.getId());

		// Then
		// 기존 GroupMembership의 상태가 PENDING으로 변경되었는지 확인
		GroupMembership updatedMembership = groupMembershipRepository.findByGroupIdAndMemberIdAndDisabled(group.getId(), member.getId(), false)
			.orElseThrow();
		assertThat(updatedMembership.getStatus()).isEqualTo(MembershipStatus.PENDING);

		// MeetingApplication이 저장되었는지 확인
		List<MeetingApplication> applications = meetingApplicationRepository.findByGroupIdAndDisabled(group.getId(), false);
		assertThat(applications).isNotNull();
		assertThat(applications.size()).isEqualTo(1);

		// 저장된 MeetingApplication이 올바른 값인지 확인
		MeetingApplication application = applications.get(0);
		assertThat(application.getMember().getId()).isEqualTo(member.getId());
		assertThat(application.getGroup().getId()).isEqualTo(group.getId());
		assertThat(application.getContext()).isEqualTo(request.context());
	}

}





