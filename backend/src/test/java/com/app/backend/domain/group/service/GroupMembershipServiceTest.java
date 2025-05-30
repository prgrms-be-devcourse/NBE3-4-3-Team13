package com.app.backend.domain.group.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.app.backend.config.TestKafkaConfig;
import com.app.backend.domain.category.entity.Category;
import com.app.backend.domain.group.entity.Group;
import com.app.backend.domain.group.entity.GroupMembership;
import com.app.backend.domain.group.entity.GroupMembershipId;
import com.app.backend.domain.group.entity.GroupRole;
import com.app.backend.domain.group.entity.MembershipStatus;
import com.app.backend.domain.group.entity.RecruitStatus;
import com.app.backend.domain.group.exception.GroupErrorCode;
import com.app.backend.domain.group.exception.GroupException;
import com.app.backend.domain.group.exception.GroupMembershipErrorCode;
import com.app.backend.domain.group.exception.GroupMembershipException;
import com.app.backend.domain.group.supporter.SpringBootTestSupporter;
import com.app.backend.domain.member.entity.Member;
import com.app.backend.domain.member.entity.Member.Provider;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@Transactional
@Import(TestKafkaConfig.class)
class GroupMembershipServiceTest extends SpringBootTestSupporter {

    @AfterEach
    void afterEach() {
        em.flush();
        em.clear();
    }

    @Test
    @DisplayName("[성공] Group ID와 Member ID 2개(모임 관리자, 모임 참가 신청자)로 모임 신청을 허가")
    void approveJoining_acceptTrue() {
        //Given
        Category category = new Category("category");
        em.persist(category);

        Member memberLeader = Member.create("testUsernameLeader",
                                            "testPasswordLeader",
                                            "testNicknameLeader",
                                            "ROLE_USER",
                                            false,
                                            Provider.LOCAL,
                                            null);

        Member memberParticipant = Member.create("testUsernameParticipant",
                                                 "testPasswordParticipant",
                                                 "testNicknameParticipant",
                                                 "ROLE_USER",
                                                 false,
                                                 Provider.LOCAL,
                                                 null);
        em.persist(memberLeader);
        em.persist(memberParticipant);
        Long leaderId  = memberLeader.getId();
        Long joiningId = memberParticipant.getId();

        Group group = Group.Companion.of("test",
                                         "test province",
                                         "test city",
                                         "test town",
                                         "test description",
                                         RecruitStatus.RECRUITING,
                                         10,
                                         category);
        em.persist(group);
        Long groupId = group.getId();

        GroupMembership groupLeaderMembership = GroupMembership.Companion.of(memberLeader, group, GroupRole.LEADER);
        GroupMembership groupJoiningMembership =
                GroupMembership.Companion.of(memberParticipant, group, GroupRole.PARTICIPANT);
        em.persist(groupLeaderMembership);
        em.persist(groupJoiningMembership);
        afterEach();

        //When
        boolean flag = groupMembershipService.approveJoining(leaderId, groupId, joiningId, true);
        afterEach();

        //Then
        GroupMembership updatedGroupMembership =
                em.find(GroupMembership.class, new GroupMembershipId(joiningId, groupId));

        assertThat(flag).isTrue();
        assertThat(updatedGroupMembership.getStatus()).isEqualTo(MembershipStatus.APPROVED);
    }

    @Test
    @DisplayName("[성공] Group ID와 Member ID 2개(모임 관리자, 모임 참가 신청자)로 모임 신청을 거부")
    void approveJoining_acceptFalse() {
        //Given
        Category category = new Category("category");
        em.persist(category);

        Member memberLeader = Member.create("testUsernameLeader",
                                            "testPasswordLeader",
                                            "testNicknameLeader",
                                            "ROLE_USER",
                                            false,
                                            Provider.LOCAL,
                                            null);
        Member memberParticipant = Member.create("testUsernameParticipant",
                                                 "testPasswordParticipant",
                                                 "testNicknameParticipant",
                                                 "ROLE_USER",
                                                 false,
                                                 Provider.LOCAL,
                                                 null);
        em.persist(memberLeader);
        em.persist(memberParticipant);
        Long leaderId  = memberLeader.getId();
        Long joiningId = memberParticipant.getId();

        Group group = Group.Companion.of("test",
                                         "test province",
                                         "test city",
                                         "test town",
                                         "test description",
                                         RecruitStatus.RECRUITING,
                                         10,
                                         category);
        em.persist(group);
        Long groupId = group.getId();

        GroupMembership groupLeaderMembership = GroupMembership.Companion.of(memberLeader, group, GroupRole.LEADER);
        GroupMembership groupJoiningMembership =
                GroupMembership.Companion.of(memberParticipant, group, GroupRole.PARTICIPANT);
        em.persist(groupLeaderMembership);
        em.persist(groupJoiningMembership);
        afterEach();

        //When
        boolean flag = groupMembershipService.approveJoining(leaderId, groupId, joiningId, false);
        afterEach();

        //Then
        GroupMembership updatedGroupMembership =
                em.find(GroupMembership.class, new GroupMembershipId(joiningId, groupId));

        assertThat(flag).isFalse();
        assertThat(updatedGroupMembership.getStatus()).isEqualTo(MembershipStatus.REJECTED);
    }

    @Test
    @DisplayName("[예외] 존재하지 않는 Group ID로 모임 신청 허가/거부 시도")
    void approveJoining_unknownGroupId() {
        //Given
        Member memberLeader = Member.create("testUsernameLeader",
                                            "testPasswordLeader",
                                            "testNicknameLeader",
                                            "ROLE_USER",
                                            false,
                                            Provider.LOCAL,
                                            null);
        Member memberParticipant = Member.create("testUsernameParticipant",
                                                 "testPasswordParticipant",
                                                 "testNicknameParticipant",
                                                 "ROLE_USER",
                                                 false,
                                                 Provider.LOCAL,
                                                 null);
        em.persist(memberLeader);
        em.persist(memberParticipant);
        Long leaderId  = memberLeader.getId();
        Long joiningId = memberParticipant.getId();
        afterEach();

        Long unknownGroupId = 1234567890L;

        //When

        //Then
        GroupErrorCode errorCode = GroupErrorCode.GROUP_NOT_FOUND;

        assertThatThrownBy(() -> groupMembershipService.approveJoining(leaderId, unknownGroupId, joiningId, true))
                .isInstanceOf(GroupException.class)
                .hasFieldOrPropertyWithValue("domainErrorCode", errorCode)
                .hasMessage(errorCode.getMessage());
    }

    @Test
    @DisplayName("[예외] 모임의 모집 상태가 닫혀있을 때 모임 신청 허가/거부 시도")
    void approveJoining_closedGroup() {
        //Given
        Category category = new Category("category");
        em.persist(category);

        Member memberLeader = Member.create("testUsernameLeader",
                                            "testPasswordLeader",
                                            "testNicknameLeader",
                                            "ROLE_USER",
                                            false,
                                            Provider.LOCAL,
                                            null);
        Member memberParticipant = Member.create("testUsernameParticipant",
                                                 "testPasswordParticipant",
                                                 "testNicknameParticipant",
                                                 "ROLE_USER",
                                                 false,
                                                 Provider.LOCAL,
                                                 null);
        em.persist(memberLeader);
        em.persist(memberParticipant);
        Long leaderId  = memberLeader.getId();
        Long joiningId = memberParticipant.getId();

        Group group = Group.Companion.of("test",
                                         "test province",
                                         "test city",
                                         "test town",
                                         "test description",
                                         RecruitStatus.RECRUITING,
                                         10,
                                         category);
        em.persist(group);
        Long groupId = group.getId();

        GroupMembership groupLeaderMembership = GroupMembership.Companion.of(memberLeader, group, GroupRole.LEADER);
        GroupMembership groupJoiningMembership =
                GroupMembership.Companion.of(memberParticipant, group, GroupRole.PARTICIPANT);
        em.persist(groupLeaderMembership);
        em.persist(groupJoiningMembership);

        group.modifyRecruitStatus(RecruitStatus.CLOSED);
        afterEach();

        //When

        //Then
        GroupErrorCode errorCode = GroupErrorCode.GROUP_NOT_IN_RECRUITMENT_STATUS;

        assertThatThrownBy(() -> groupMembershipService.approveJoining(leaderId, groupId, joiningId, true))
                .isInstanceOf(GroupException.class)
                .hasFieldOrPropertyWithValue("domainErrorCode", errorCode)
                .hasMessage(errorCode.getMessage());
    }

    @Test
    @DisplayName("[예외] 모임이 최대 가입 한도에 도달한 상태에서 모임 신청 허가/거부 시도")
    void approveJoining_maximumMembers() {
        //Given
        Category category = new Category("category");
        em.persist(category);

        Member memberLeader = Member.create("testUsernameLeader",
                                            "testPasswordLeader",
                                            "testNicknameLeader",
                                            "ROLE_USER",
                                            false,
                                            Provider.LOCAL,
                                            null);
        Member memberParticipant = Member.create("testUsernameParticipant",
                                                 "testPasswordParticipant",
                                                 "testNicknameParticipant",
                                                 "ROLE_USER",
                                                 false,
                                                 Provider.LOCAL,
                                                 null);
        em.persist(memberLeader);
        em.persist(memberParticipant);
        Long leaderId  = memberLeader.getId();
        Long joiningId = memberParticipant.getId();

        Group group = Group.Companion.of("test",
                                         "test province",
                                         "test city",
                                         "test town",
                                         "test description",
                                         RecruitStatus.RECRUITING,
                                         1,
                                         category);
        em.persist(group);
        Long groupId = group.getId();

        GroupMembership groupLeaderMembership = GroupMembership.Companion.of(memberLeader, group, GroupRole.LEADER);
        GroupMembership groupJoiningMembership =
                GroupMembership.Companion.of(memberParticipant, group, GroupRole.PARTICIPANT);
        em.persist(groupLeaderMembership);
        em.persist(groupJoiningMembership);
        afterEach();

        //When

        //Then
        GroupErrorCode errorCode = GroupErrorCode.GROUP_MAXIMUM_NUMBER_OF_MEMBERS;

        assertThatThrownBy(() -> groupMembershipService.approveJoining(leaderId, groupId, joiningId, true))
                .isInstanceOf(GroupException.class)
                .hasFieldOrPropertyWithValue("domainErrorCode", errorCode)
                .hasMessage(errorCode.getMessage());
    }

    @Test
    @DisplayName("[예외] 모임 가입 신청 승인/거절을 모임의 관리자 권한이 없는 회원이 시도")
    void approveJoining_notALeader() {
        //Given
        Category category = new Category("category");
        em.persist(category);

        Member memberFakeLeader = Member.create("testUsernameLeader",
                                                "testPasswordLeader",
                                                "testNicknameLeader",
                                                "ROLE_USER",
                                                false,
                                                Provider.LOCAL,
                                                null);
        Member memberParticipant = Member.create("testUsernameParticipant",
                                                 "testPasswordParticipant",
                                                 "testNicknameParticipant",
                                                 "ROLE_USER",
                                                 false,
                                                 Provider.LOCAL,
                                                 null);
        em.persist(memberFakeLeader);
        em.persist(memberParticipant);
        Long fakeLeaderId = memberFakeLeader.getId();
        Long joiningId    = memberParticipant.getId();

        Group group = Group.Companion.of("test",
                                         "test province",
                                         "test city",
                                         "test town",
                                         "test description",
                                         RecruitStatus.RECRUITING,
                                         10,
                                         category);
        em.persist(group);
        Long groupId = group.getId();

        GroupMembership groupFakeLeaderMembership =
                GroupMembership.Companion.of(memberFakeLeader, group, GroupRole.PARTICIPANT);
        GroupMembership groupJoiningMembership =
                GroupMembership.Companion.of(memberParticipant, group, GroupRole.PARTICIPANT);
        em.persist(groupFakeLeaderMembership);
        em.persist(groupJoiningMembership);
        afterEach();

        //When

        //Then
        GroupMembershipErrorCode errorCode = GroupMembershipErrorCode.GROUP_MEMBERSHIP_NO_PERMISSION;

        assertThatThrownBy(() -> groupMembershipService.approveJoining(fakeLeaderId, groupId, joiningId, true))
                .isInstanceOf(GroupMembershipException.class)
                .hasFieldOrPropertyWithValue("domainErrorCode", errorCode)
                .hasMessage(errorCode.getMessage());
    }

    @Test
    @DisplayName("[예외] 모임 가입을 희망하는 회원이 이미 가입/탈퇴한 상태인 경우")
    void approveJoining_unacceptableStatus() {
        //Given
        Category category = new Category("category");
        em.persist(category);

        Member memberLeader = Member.create("testUsernameLeader",
                                            "testPasswordLeader",
                                            "testNicknameLeader",
                                            "ROLE_USER",
                                            false,
                                            Provider.LOCAL,
                                            null);
        Member memberParticipant = Member.create("testUsernameParticipant",
                                                 "testPasswordParticipant",
                                                 "testNicknameParticipant",
                                                 "ROLE_USER",
                                                 false,
                                                 Provider.LOCAL,
                                                 null);
        em.persist(memberLeader);
        em.persist(memberParticipant);
        Long leaderId  = memberLeader.getId();
        Long joiningId = memberParticipant.getId();

        Group group = Group.Companion.of("test",
                                         "test province",
                                         "test city",
                                         "test town",
                                         "test description",
                                         RecruitStatus.RECRUITING,
                                         10,
                                         category);
        em.persist(group);
        Long groupId = group.getId();

        GroupMembership groupLeaderMembership = GroupMembership.Companion.of(memberLeader, group, GroupRole.LEADER);
        GroupMembership groupJoiningMembership =
                GroupMembership.Companion.of(memberParticipant, group, GroupRole.PARTICIPANT);
        em.persist(groupLeaderMembership);
        em.persist(groupJoiningMembership);

        groupJoiningMembership.modifyStatus(MembershipStatus.APPROVED);
        afterEach();

        //When

        //Then
        GroupMembershipErrorCode errorCode = GroupMembershipErrorCode.GROUP_MEMBERSHIP_UNACCEPTABLE_STATUS;

        assertThatThrownBy(() -> groupMembershipService.approveJoining(leaderId, groupId, joiningId, true))
                .isInstanceOf(GroupMembershipException.class)
                .hasFieldOrPropertyWithValue("domainErrorCode", errorCode)
                .hasMessage(errorCode.getMessage());
    }

    @Test
    @DisplayName("[성공] 모임의 관리자가 모임 내 특정 회원의 권한을 변경(LEADER <-> PARTICIPANT)")
    void modifyGroupRole() {
        //Given
        Category category = new Category("category");
        em.persist(category);

        Member memberLeader = Member.create("testUsernameLeader",
                                            "testPasswordLeader",
                                            "testNicknameLeader",
                                            "ROLE_USER",
                                            false,
                                            Provider.LOCAL,
                                            null);
        Member memberParticipant = Member.create("testUsernameParticipant",
                                                 "testPasswordParticipant",
                                                 "testNicknameParticipant",
                                                 "ROLE_USER",
                                                 false,
                                                 Provider.LOCAL,
                                                 null);
        em.persist(memberLeader);
        em.persist(memberParticipant);
        Long leaderId = memberLeader.getId();
        Long memberId = memberParticipant.getId();

        Group group = Group.Companion.of("test",
                                         "test province",
                                         "test city",
                                         "test town",
                                         "test description",
                                         RecruitStatus.RECRUITING,
                                         10,
                                         category);
        em.persist(group);
        Long groupId = group.getId();

        GroupMembership groupLeaderMembership = GroupMembership.Companion.of(memberLeader, group, GroupRole.LEADER);
        GroupMembership groupMembership =
                GroupMembership.Companion.of(memberParticipant, group, GroupRole.PARTICIPANT);
        em.persist(groupLeaderMembership);
        em.persist(groupMembership);

        groupMembership.modifyStatus(MembershipStatus.APPROVED);
        afterEach();

        //When
        boolean flag = groupMembershipService.modifyGroupRole(leaderId, groupId, memberId);
        afterEach();

        //Then
        GroupMembership updatedGroupMembership =
                em.find(GroupMembership.class, new GroupMembershipId(memberId, groupId));

        assertThat(updatedGroupMembership.getGroupRole()).isNotEqualTo(GroupRole.PARTICIPANT);
        assertThat(updatedGroupMembership.getGroupRole()).isEqualTo(GroupRole.LEADER);
    }

    @Test
    @DisplayName("[예외] 존재하지 않는 Group ID로 회원 권한 변경 시도")
    void modifyGroupRole_unknownGroupId() {
        //Given
        Member memberLeader = Member.create("testUsernameLeader",
                                            "testPasswordLeader",
                                            "testNicknameLeader",
                                            "ROLE_USER",
                                            false,
                                            Provider.LOCAL,
                                            null);
        Member memberParticipant = Member.create("testUsernameParticipant",
                                                 "testPasswordParticipant",
                                                 "testNicknameParticipant",
                                                 "ROLE_USER",
                                                 false,
                                                 Provider.LOCAL,
                                                 null);
        em.persist(memberLeader);
        em.persist(memberParticipant);
        Long leaderId = memberLeader.getId();
        Long memberId = memberParticipant.getId();

        Long unknownGroupId = 1234567890L;
        afterEach();

        //When

        //Then
        GroupMembershipErrorCode errorCode = GroupMembershipErrorCode.GROUP_MEMBERSHIP_NOT_FOUND;

        assertThatThrownBy(() -> groupMembershipService.modifyGroupRole(leaderId, unknownGroupId, memberId))
                .isInstanceOf(GroupMembershipException.class)
                .hasFieldOrPropertyWithValue("domainErrorCode", errorCode)
                .hasMessage(errorCode.getMessage());
    }

    @Test
    @DisplayName("[예외] 존재하지 않는 Member ID(모임 관리자)로 회원 권한 변경 시도")
    void modifyGroupRole_unknownLeaderId() {
        //Given
        Category category = new Category("category");
        em.persist(category);

        Member memberLeader = Member.create("testUsernameLeader",
                                            "testPasswordLeader",
                                            "testNicknameLeader",
                                            "ROLE_USER",
                                            false,
                                            Provider.LOCAL,
                                            null);
        Member memberParticipant = Member.create("testUsernameParticipant",
                                                 "testPasswordParticipant",
                                                 "testNicknameParticipant",
                                                 "ROLE_USER",
                                                 false,
                                                 Provider.LOCAL,
                                                 null);
        em.persist(memberLeader);
        em.persist(memberParticipant);
        Long memberId = memberParticipant.getId();

        Group group = Group.Companion.of("test",
                                         "test province",
                                         "test city",
                                         "test town",
                                         "test description",
                                         RecruitStatus.RECRUITING,
                                         10,
                                         category);
        em.persist(group);
        Long groupId = group.getId();

        GroupMembership groupLeaderMembership = GroupMembership.Companion.of(memberLeader, group, GroupRole.LEADER);
        GroupMembership groupMembership =
                GroupMembership.Companion.of(memberParticipant, group, GroupRole.PARTICIPANT);
        em.persist(groupLeaderMembership);
        em.persist(groupMembership);

        groupMembership.modifyStatus(MembershipStatus.APPROVED);
        afterEach();

        Long unknownLeaderId = 1234567890L;

        //When

        //Then
        GroupMembershipErrorCode errorCode = GroupMembershipErrorCode.GROUP_MEMBERSHIP_NOT_FOUND;

        assertThatThrownBy(() -> groupMembershipService.modifyGroupRole(unknownLeaderId, groupId, memberId))
                .isInstanceOf(GroupMembershipException.class)
                .hasFieldOrPropertyWithValue("domainErrorCode", errorCode)
                .hasMessage(errorCode.getMessage());
    }

    @Test
    @DisplayName("[예외] 모임 내 회원 권한 변경을 모임의 관리자 권한이 없는 회원이 시도")
    void modifyGroupRole_notALeader() {
        //Given
        Category category = new Category("category");
        em.persist(category);

        Member memberFakeLeader = Member.create("testUsernameLeader",
                                                "testPasswordLeader",
                                                "testNicknameLeader",
                                                "ROLE_USER",
                                                false,
                                                Provider.LOCAL,
                                                null);
        Member memberParticipant = Member.create("testUsernameParticipant",
                                                 "testPasswordParticipant",
                                                 "testNicknameParticipant",
                                                 "ROLE_USER",
                                                 false,
                                                 Provider.LOCAL,
                                                 null);
        em.persist(memberFakeLeader);
        em.persist(memberParticipant);
        Long fakeLeaderId = memberFakeLeader.getId();
        Long memberId     = memberParticipant.getId();

        Group group = Group.Companion.of("test",
                                         "test province",
                                         "test city",
                                         "test town",
                                         "test description",
                                         RecruitStatus.RECRUITING,
                                         10,
                                         category);
        em.persist(group);
        Long groupId = group.getId();

        GroupMembership groupFakeLeaderMembership =
                GroupMembership.Companion.of(memberFakeLeader, group, GroupRole.PARTICIPANT);
        GroupMembership groupMembership =
                GroupMembership.Companion.of(memberParticipant, group, GroupRole.PARTICIPANT);
        em.persist(groupFakeLeaderMembership);
        em.persist(groupMembership);
        afterEach();

        //When

        //Then
        GroupMembershipErrorCode errorCode = GroupMembershipErrorCode.GROUP_MEMBERSHIP_NO_PERMISSION;

        assertThatThrownBy(() -> groupMembershipService.modifyGroupRole(fakeLeaderId, groupId, memberId))
                .isInstanceOf(GroupMembershipException.class)
                .hasFieldOrPropertyWithValue("domainErrorCode", errorCode)
                .hasMessage(errorCode.getMessage());
    }

    @Test
    @DisplayName("[예외] 존재하지 않는 Member ID(모임 회원)의 회원 권한 변경 시도")
    void modifyGroupRole_unknownMemberId() {
        //Given
        Category category = new Category("category");
        em.persist(category);

        Member memberLeader = Member.create("testUsernameLeader",
                                            "testPasswordLeader",
                                            "testNicknameLeader",
                                            "ROLE_USER",
                                            false,
                                            Provider.LOCAL,
                                            null);
        Member memberParticipant = Member.create("testUsernameParticipant",
                                                 "testPasswordParticipant",
                                                 "testNicknameParticipant",
                                                 "ROLE_USER",
                                                 false,
                                                 Provider.LOCAL,
                                                 null);
        em.persist(memberLeader);
        em.persist(memberParticipant);
        Long leaderId = memberLeader.getId();

        Group group = Group.Companion.of("test",
                                         "test province",
                                         "test city",
                                         "test town",
                                         "test description",
                                         RecruitStatus.RECRUITING,
                                         10,
                                         category);
        em.persist(group);
        Long groupId = group.getId();

        GroupMembership groupLeaderMembership = GroupMembership.Companion.of(memberLeader, group, GroupRole.LEADER);
        GroupMembership groupMembership =
                GroupMembership.Companion.of(memberParticipant, group, GroupRole.PARTICIPANT);
        em.persist(groupLeaderMembership);
        em.persist(groupMembership);

        groupMembership.modifyStatus(MembershipStatus.APPROVED);
        afterEach();

        Long unknownMemberId = 1234567890L;

        //When

        //Then
        GroupMembershipErrorCode errorCode = GroupMembershipErrorCode.GROUP_MEMBERSHIP_NOT_FOUND;

        assertThatThrownBy(() -> groupMembershipService.modifyGroupRole(leaderId, groupId, unknownMemberId))
                .isInstanceOf(GroupMembershipException.class)
                .hasFieldOrPropertyWithValue("domainErrorCode", errorCode)
                .hasMessage(errorCode.getMessage());
    }

    @Test
    @DisplayName("[예외] 모임 가입 상태가 아닌 회원의 모임 권한을 변경 시도")
    void modifyGroupRole_notChangeableState() {
        //Given
        Category category = new Category("category");
        em.persist(category);

        Member memberLeader = Member.create("testUsernameLeader",
                                            "testPasswordLeader",
                                            "testNicknameLeader",
                                            "ROLE_USER",
                                            false,
                                            Provider.LOCAL,
                                            null);
        Member memberParticipant = Member.create("testUsernameParticipant",
                                                 "testPasswordParticipant",
                                                 "testNicknameParticipant",
                                                 "ROLE_USER",
                                                 false,
                                                 Provider.LOCAL,
                                                 null);
        em.persist(memberLeader);
        em.persist(memberParticipant);
        Long leaderId = memberLeader.getId();
        Long memberId = memberParticipant.getId();

        Group group = Group.Companion.of("test",
                                         "test province",
                                         "test city",
                                         "test town",
                                         "test description",
                                         RecruitStatus.RECRUITING,
                                         10,
                                         category);
        em.persist(group);
        Long groupId = group.getId();

        GroupMembership groupLeaderMembership = GroupMembership.Companion.of(memberLeader, group, GroupRole.LEADER);
        GroupMembership groupMembership =
                GroupMembership.Companion.of(memberParticipant, group, GroupRole.PARTICIPANT);
        em.persist(groupLeaderMembership);
        em.persist(groupMembership);
        afterEach();

        //When

        //Then
        GroupMembershipErrorCode errorCode = GroupMembershipErrorCode.GROUP_MEMBERSHIP_GROUP_ROLE_NOT_CHANGEABLE_STATE;

        assertThatThrownBy(() -> groupMembershipService.modifyGroupRole(leaderId, groupId, memberId))
                .isInstanceOf(GroupMembershipException.class)
                .hasFieldOrPropertyWithValue("domainErrorCode", errorCode)
                .hasMessage(errorCode.getMessage());
    }

    @Test
    @DisplayName("[성공] 모임에서 탈퇴")
    void leaveGroup() {
        //Given
        Category category = new Category("category");
        em.persist(category);

        Member memberLeader = Member.create("testUsernameLeader",
                                            "testPasswordLeader",
                                            "testNicknameLeader",
                                            "ROLE_USER",
                                            false,
                                            Provider.LOCAL,
                                            null);
        Member memberParticipant = Member.create("testUsernameParticipant",
                                                 "testPasswordParticipant",
                                                 "testNicknameParticipant",
                                                 "ROLE_USER",
                                                 false,
                                                 Provider.LOCAL,
                                                 null);
        em.persist(memberLeader);
        em.persist(memberParticipant);
        Long memberId = memberParticipant.getId();

        Group group = Group.Companion.of("test",
                                         "test province",
                                         "test city",
                                         "test town",
                                         "test description",
                                         RecruitStatus.RECRUITING,
                                         10,
                                         category);
        em.persist(group);
        Long groupId = group.getId();

        GroupMembership groupLeaderMembership = GroupMembership.Companion.of(memberLeader, group, GroupRole.LEADER);
        GroupMembership groupMembership =
                GroupMembership.Companion.of(memberParticipant, group, GroupRole.PARTICIPANT);
        em.persist(groupLeaderMembership);
        em.persist(groupMembership);

        groupMembership.modifyStatus(MembershipStatus.APPROVED);
        afterEach();

        //When
        boolean flag = groupMembershipService.leaveGroup(groupId, memberId);
        afterEach();

        //Then
        GroupMembership leaveGroupMembership = em.find(GroupMembership.class, new GroupMembershipId(memberId, groupId));

        assertThat(flag).isTrue();
        assertThat(leaveGroupMembership.getStatus()).isEqualTo(MembershipStatus.LEAVE);
    }

    @Test
    @DisplayName("[예외] 존재하지 않는 Group ID로 모임 탈퇴 시도")
    void leaveGroup_unknownGroupId() {
        //Given
        Member memberLeader = Member.create("testUsernameLeader",
                                            "testPasswordLeader",
                                            "testNicknameLeader",
                                            "ROLE_USER",
                                            false,
                                            Provider.LOCAL,
                                            null);
        Member memberParticipant = Member.create("testUsernameParticipant",
                                                 "testPasswordParticipant",
                                                 "testNicknameParticipant",
                                                 "ROLE_USER",
                                                 false,
                                                 Provider.LOCAL,
                                                 null);
        em.persist(memberLeader);
        em.persist(memberParticipant);
        Long memberId = memberParticipant.getId();
        afterEach();

        Long unknownGroupId = 1234567890L;

        //When

        //Then
        GroupMembershipErrorCode errorCode = GroupMembershipErrorCode.GROUP_MEMBERSHIP_NOT_FOUND;

        assertThatThrownBy(() -> groupMembershipService.leaveGroup(unknownGroupId, memberId))
                .isInstanceOf(GroupMembershipException.class)
                .hasFieldOrPropertyWithValue("domainErrorCode", errorCode)
                .hasMessage(errorCode.getMessage());
    }

    @Test
    @DisplayName("[예외] 존재하지 않는 Member ID로 모임 탈퇴 시도")
    void leaveGroup_unknownMemberId() {
        //Given
        Category category = new Category("category");
        em.persist(category);

        Group group = Group.Companion.of("test",
                                         "test province",
                                         "test city",
                                         "test town",
                                         "test description",
                                         RecruitStatus.RECRUITING,
                                         10,
                                         category);
        em.persist(group);
        Long groupId = group.getId();
        afterEach();

        Long unknownMemberId = 1234567890L;

        //When

        //Then
        GroupMembershipErrorCode errorCode = GroupMembershipErrorCode.GROUP_MEMBERSHIP_NOT_FOUND;

        assertThatThrownBy(() -> groupMembershipService.leaveGroup(groupId, unknownMemberId))
                .hasFieldOrPropertyWithValue("domainErrorCode", errorCode)
                .hasMessage(errorCode.getMessage());
    }

    @Test
    @DisplayName("[예외] 모임 관리자가 1명뿐인 모임에서 관리자가 탈퇴를 시도하는 경우")
    void leaveGroup_unableToLeave() {
        //Given
        Category category = new Category("category");
        em.persist(category);

        Member memberLeader = Member.create("testUsernameLeader",
                                            "testPasswordLeader",
                                            "testNicknameLeader",
                                            "ROLE_USER",
                                            false,
                                            Provider.LOCAL,
                                            null);
        em.persist(memberLeader);
        Long leaderId = memberLeader.getId();

        Group group = Group.Companion.of("test",
                                         "test province",
                                         "test city",
                                         "test town",
                                         "test description",
                                         RecruitStatus.RECRUITING,
                                         10,
                                         category);
        em.persist(group);
        Long groupId = group.getId();

        GroupMembership groupLeaderMembership = GroupMembership.Companion.of(memberLeader, group, GroupRole.LEADER);
        em.persist(groupLeaderMembership);
        afterEach();

        //When

        //Then
        GroupMembershipErrorCode errorCode = GroupMembershipErrorCode.GROUP_MEMBERSHIP_UNABLE_TO_LEAVE;

        assertThatThrownBy(() -> groupMembershipService.leaveGroup(groupId, leaderId))
                .hasFieldOrPropertyWithValue("domainErrorCode", errorCode)
                .hasMessage(errorCode.getMessage());
    }

}