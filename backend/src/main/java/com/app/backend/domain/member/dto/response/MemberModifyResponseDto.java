package com.app.backend.domain.member.dto.response;

import com.app.backend.domain.member.entity.Member;

public record MemberModifyResponseDto(
	Long id,
	String username,
	String password,
	String nickname,
	String role,
	boolean disabled
) {

	public static MemberModifyResponseDto of(Member savedMember) {
		return new MemberModifyResponseDto(
			savedMember.getId(),
			savedMember.getUsername(),
			savedMember.getPassword(),
			savedMember.getNickname(),
			savedMember.getRole(),
			savedMember.isDisabled()
		);
	}
}
