package com.app.backend.domain.meetingApplication.controller

import com.app.backend.domain.meetingApplication.dto.MeetingApplicationDto
import com.app.backend.domain.meetingApplication.dto.MeetingApplicationReqBody
import com.app.backend.domain.meetingApplication.dto.response.MeetingApplicationResponse
import com.app.backend.domain.meetingApplication.service.MeetingApplicationService
import com.app.backend.domain.member.entity.MemberDetails
import com.app.backend.global.annotation.CustomPageJsonSerializer
import com.app.backend.global.dto.response.ApiResponse
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.web.PageableDefault
import org.springframework.http.HttpStatus
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/v1/groups")
class MeetingApplicationController(
    private val meetingApplicationService: MeetingApplicationService
) {

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping("/{groupId}")
    fun createMeetingApplication(
        @PathVariable groupId: Long,
        @RequestBody request: MeetingApplicationReqBody,
        @AuthenticationPrincipal memberDetails: MemberDetails
    ): ApiResponse<MeetingApplicationDto> {
        meetingApplicationService.validateGroupMemberLimit(groupId) // 인원 제한 검증

        val meetingApplication = meetingApplicationService.create(groupId, request, memberDetails.id)

        val meetingApplicationDto = MeetingApplicationDto.invoke(meetingApplication)

        return ApiResponse.of(
            true,
            "201",
            "${meetingApplication.group.id}번 모임에 성공적으로 가입 신청을 하셨습니다.",
            meetingApplicationDto
        )
    }

    @GetMapping("/{groupId}/meeting_applications")
    @CustomPageJsonSerializer(hasContent = false, size = false, isFirst = false, isLast = false, sort = false, empty = false)
    fun getMeetingApplications(
        @PathVariable groupId: Long,
        @PageableDefault(size = 10, page = 0) pageable: Pageable,
        @AuthenticationPrincipal memberDetails: MemberDetails
    ): ApiResponse<Page<MeetingApplicationResponse.Detail>> {
        val applications = meetingApplicationService.getMeetingApplications(groupId, memberDetails.id, pageable)

        return ApiResponse.of(
            true,
            HttpStatus.OK,
            "meeting application 조회 성공",
            applications
        )
    }

    @GetMapping("/{groupId}/meeting_applications/{meetingApplicationId}")
    fun getMeetingApplication(
        @PathVariable groupId: Long,
        @PathVariable meetingApplicationId: Long,
        @AuthenticationPrincipal memberDetails: MemberDetails
    ): ApiResponse<MeetingApplicationResponse.Detail> {
        val meetingApplicationDetail = meetingApplicationService.getMeetingApplicationById(groupId, meetingApplicationId, memberDetails.id)

        return ApiResponse.of(
            true,
            HttpStatus.OK,
            "meeting application 상세 조회 성공",
            meetingApplicationDetail
        )
    }
}
