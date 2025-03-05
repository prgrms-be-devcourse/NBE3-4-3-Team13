package com.app.backend.domain.member.dto.response;

import com.app.backend.domain.group.entity.GroupRole;

public record MemberChatResponseDto(Long memberId, String memberNickname, GroupRole groupRole) {

}
