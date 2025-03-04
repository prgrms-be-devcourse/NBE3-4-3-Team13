package com.app.backend.domain.meetingApplication.dto;

import jakarta.validation.constraints.NotBlank;

public record MeetingApplicationReqBody(
	@NotBlank
	String context
) {
}
