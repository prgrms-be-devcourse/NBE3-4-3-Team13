package com.app.backend.domain.group.dto.response;

import com.app.backend.domain.group.entity.Group;
import com.app.backend.domain.group.entity.GroupRole;
import com.app.backend.domain.group.entity.MembershipStatus;
import com.app.backend.global.util.AppUtil;
import java.util.List;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

public class GroupResponse {

    public static Detail toDetail(final Group group) {
        return Detail.builder()
                     .id(group.getId())
                     .categoryName(group.getCategory().getName())
                     .name(group.getName())
                     .province(group.getProvince())
                     .city(group.getCity())
                     .town(group.getTown())
                     .description(group.getDescription())
                     .recruitStatus(group.getRecruitStatus().name())
                     .maxRecruitCount(group.getMaxRecruitCount())
                     .currentMemberCount(Math.toIntExact(
                             group.getMembers().stream().filter(m -> m.getStatus() == MembershipStatus.APPROVED
                                                                     && !m.getDisabled()).count()
                     )).createdAt(AppUtil.localDateTimeToString(group.getCreatedAt()))
                     .groupLeaders(group.getMembers().stream().filter(m -> m.getStatus() == MembershipStatus.APPROVED
                                                                           && m.getGroupRole() == GroupRole.LEADER
                                                                           && !m.getDisabled())
                                        .map(m -> m.getMember().getNickname()).toList())
                     .build();
    }

    public static Detail toDetail(final Group group,
                                  final boolean isApplying,
                                  final boolean isMember,
                                  final boolean isAdmin) {
        return Detail.builder()
                     .id(group.getId())
                     .categoryName(group.getCategory().getName())
                     .name(group.getName())
                     .province(group.getProvince())
                     .city(group.getCity())
                     .town(group.getTown())
                     .description(group.getDescription())
                     .recruitStatus(group.getRecruitStatus().name())
                     .maxRecruitCount(group.getMaxRecruitCount())
                     .currentMemberCount(Math.toIntExact(
                             group.getMembers().stream().filter(m -> m.getStatus() == MembershipStatus.APPROVED
                                                                     && !m.getDisabled()).count()
                     )).createdAt(AppUtil.localDateTimeToString(group.getCreatedAt()))
                     .groupLeaders(group.getMembers().stream().filter(m -> m.getStatus() == MembershipStatus.APPROVED
                                                                           && m.getGroupRole() == GroupRole.LEADER
                                                                           && !m.getDisabled())
                                        .map(m -> m.getMember().getNickname()).toList())
                     .isApplying(isApplying)
                     .isMember(isMember)
                     .isAdmin(isAdmin)
                     .build();
    }

    public static ListInfo toListInfo(final Group group) {
        return ListInfo.builder()
                       .id(group.getId())
                       .categoryName(group.getCategory().getName())
                       .name(group.getName())
                       .province(group.getProvince())
                       .city(group.getCity())
                       .town(group.getTown())
                       .recruitStatus(group.getRecruitStatus().name())
                       .maxRecruitCount(group.getMaxRecruitCount())
                       .currentMemberCount(Math.toIntExact(
                               group.getMembers().stream().filter(m -> m.getStatus() == MembershipStatus.APPROVED
                                                                       && !m.getDisabled()).count()
                       )).createdAt(AppUtil.localDateTimeToString(group.getCreatedAt()))
                       .groupLeaders(group.getMembers().stream().filter(m -> m.getStatus() == MembershipStatus.APPROVED
                                                                             && m.getGroupRole() == GroupRole.LEADER
                                                                             && !m.getDisabled())
                                          .map(m -> m.getMember().getNickname()).toList())
                       .build();
    }

    @Getter
    @Builder(access = AccessLevel.PRIVATE)
    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    public static class Detail {
        private final Long         id;
        private final String       categoryName;
        private final String       name;
        private final String       province;
        private final String       city;
        private final String       town;
        private final String       description;
        private final String       recruitStatus;
        private final Integer      maxRecruitCount;
        private final Integer      currentMemberCount;
        private final String       createdAt;
        private final Boolean      isApplying;
        private final Boolean      isMember;
        private final Boolean      isAdmin;
        private final List<String> groupLeaders;
    }

    @Getter
    @Builder(access = AccessLevel.PRIVATE)
    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    public static class ListInfo {
        private final Long         id;
        private final String       categoryName;
        private final String       name;
        private final String       province;
        private final String       city;
        private final String       town;
        private final String       recruitStatus;
        private final Integer      maxRecruitCount;
        private final Integer      currentMemberCount;
        private final String       createdAt;
        private final List<String> groupLeaders;
    }

}
