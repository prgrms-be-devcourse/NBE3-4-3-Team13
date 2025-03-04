package com.app.backend.domain.meetingApplication.dto;

import com.app.backend.domain.meetingApplication.entity.MeetingApplication;

public record MeetingApplicationDto(
	Long id,
	Long memberId,
	Long groupId,
	String context
) {
	public static MeetingApplicationDto from(MeetingApplication meetingApplication) {
		return new MeetingApplicationDto(
			meetingApplication.getId(),
			meetingApplication.getMember().getId(),
			meetingApplication.getGroup().getId(),
			meetingApplication.getContext()
		);
	}
}
