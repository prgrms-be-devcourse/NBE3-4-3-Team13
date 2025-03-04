package com.app.backend.domain.member.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;

public record MemberModifyRequestDto(
	@Schema(
		description = "닉네임",
		example = "새로운닉네임",
		nullable = true
	)
	String nickname,
	@Schema(
		description = "비밀번호",
		example = "새로운비밀번호",
		nullable = true
	)
	String password
) {
}
