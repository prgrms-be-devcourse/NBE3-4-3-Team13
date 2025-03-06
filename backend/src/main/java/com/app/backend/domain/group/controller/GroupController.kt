package com.app.backend.domain.group.controller

import com.app.backend.domain.group.constant.GroupMessageConstant
import com.app.backend.domain.group.dto.request.GroupRequest
import com.app.backend.domain.group.dto.response.GroupResponse
import com.app.backend.domain.group.exception.GroupException
import com.app.backend.domain.group.exception.GroupMembershipException
import com.app.backend.domain.group.service.GroupMembershipService
import com.app.backend.domain.group.service.GroupService
import com.app.backend.domain.member.entity.MemberDetails
import com.app.backend.global.annotation.CustomPageJsonSerializer
import com.app.backend.global.dto.response.ApiResponse
import com.app.backend.global.error.exception.GlobalErrorCode
import jakarta.validation.Valid
import jakarta.validation.constraints.Min
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.data.web.PageableDefault
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.validation.BindingResult
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping(
    value = ["/api/v1/groups"],
    consumes = [MediaType.APPLICATION_JSON_VALUE],
    produces = [MediaType.APPLICATION_JSON_VALUE]
)
class GroupController(
    private val groupService: GroupService,
    private val groupMembershipService: GroupMembershipService
) {
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun createGroup(
        @RequestBody @Valid requestDto: GroupRequest.Create,
        bindingResult: BindingResult,
        @AuthenticationPrincipal userDetails: UserDetails
    ): ApiResponse<Void> {
        if (bindingResult.hasErrors())
            throw GroupException(GlobalErrorCode.INVALID_INPUT_VALUE)
        groupService.createGroup((userDetails as MemberDetails).id!!, requestDto)
        return ApiResponse.of(true, HttpStatus.CREATED, GroupMessageConstant.CREATE_GROUP_SUCCESS)
    }

    @GetMapping("/{groupId}")
    fun getGroupById(
        @PathVariable @Min(1) groupId: Long,
        @AuthenticationPrincipal userDetails: UserDetails
    ): ApiResponse<GroupResponse.Detail> =
        ApiResponse.of(
            true,
            HttpStatus.OK,
            GroupMessageConstant.READ_GROUP_SUCCESS,
            groupService.getGroup(groupId, (userDetails as MemberDetails).id!!)
        )

    @GetMapping
    @CustomPageJsonSerializer(
        hasContent = false,
        size = false,
        isFirst = false,
        isLast = false,
        sort = false,
        empty = false
    )
    fun getGroups(
        @RequestParam(required = false) categoryName: String,
        @RequestParam(required = false) recruitStatus: String,
        @RequestParam(required = false) province: String,
        @RequestParam(required = false) city: String,
        @RequestParam(required = false) town: String,
        @RequestParam(required = false) keyword: String,
        @PageableDefault(
            size = 10,
            page = 0,
            sort = ["createdAt"],
            direction = Sort.Direction.DESC
        ) pageable: Pageable
    ): ApiResponse<Page<GroupResponse.ListInfo>> =
        ApiResponse.of(
            true, HttpStatus.OK, GroupMessageConstant.READ_GROUPS_SUCCESS, groupService.getGroupsBySearch(
                GroupRequest.Search(
                    categoryName,
                    recruitStatus, keyword, province, city, town
                ), pageable
            )
        )

    @PatchMapping("/{groupId}")
    fun modifyGroup(
        @PathVariable @Min(1) groupId: Long,
        @RequestBody @Valid requestDto: GroupRequest.Update,
        bindingResult: BindingResult,
        @AuthenticationPrincipal userDetails: UserDetails
    ): ApiResponse<Unit> {
        if (bindingResult.hasErrors()) throw GroupException(GlobalErrorCode.INVALID_INPUT_VALUE)
        groupService.modifyGroup(groupId, (userDetails as MemberDetails).id!!, requestDto)
        return ApiResponse.of(true, HttpStatus.OK, GroupMessageConstant.UPDATE_GROUP_SUCCESS)
    }

    @DeleteMapping("/{groupId}")
    fun deleteGroup(
        @PathVariable @Min(1) groupId: Long,
        @AuthenticationPrincipal userDetails: UserDetails
    ): ApiResponse<Unit> =
        if (!(groupService.deleteGroup(groupId, (userDetails as MemberDetails).id!!)))
            throw GroupException(GlobalErrorCode.INTERNAL_SERVER_ERROR)
        else ApiResponse.of(true, HttpStatus.OK, GroupMessageConstant.DELETE_GROUP_SUCCESS)

    @DeleteMapping("/{groupId}/leave")
    fun leaveGroup(
        @PathVariable @Min(1) groupId: Long,
        @AuthenticationPrincipal userDetails: UserDetails
    ): ApiResponse<Unit> =
        if (!(groupMembershipService.leaveGroup(groupId, (userDetails as MemberDetails).id!!)))
            throw GroupMembershipException(GlobalErrorCode.INTERNAL_SERVER_ERROR)
        else ApiResponse.of(true, HttpStatus.OK, GroupMessageConstant.LEAVE_GROUP_SUCCESS)

    @PostMapping("/{groupId}/approve")
    fun approveJoining(
        @PathVariable @Min(1) groupId: Long,
        @RequestBody @Valid requestDto: GroupRequest.ApproveJoining,
        bindingResult: BindingResult,
        @AuthenticationPrincipal userDetails: UserDetails
    ): ApiResponse<Unit> {
        if (bindingResult.hasErrors())
            throw GroupMembershipException(GlobalErrorCode.INVALID_INPUT_VALUE)

        return if (groupMembershipService.approveJoining(
                (userDetails as MemberDetails).id!!,
                groupId,
                requestDto.memberId,
                requestDto.isAccept
            )
        )
            ApiResponse.of(true, HttpStatus.OK, GroupMessageConstant.APPROVE_JOINING_SUCCESS)
        else
            ApiResponse.of(true, HttpStatus.OK, GroupMessageConstant.REJECT_JOINING_SUCCESS)
    }

    @PatchMapping("/{groupId}/permission")
    fun modifyGroupRole(
        @PathVariable @Min(1) groupId: Long,
        @RequestBody @Valid requestDto: GroupRequest.Permission,
        bindingResult: BindingResult,
        @AuthenticationPrincipal userDetails: UserDetails
    ): ApiResponse<Unit> {
        if (bindingResult.hasErrors())
            throw GroupMembershipException(GlobalErrorCode.INVALID_INPUT_VALUE)

        if (
            !groupMembershipService.modifyGroupRole(
                (userDetails as MemberDetails).id!!,
                groupId,
                requestDto.memberId
            )
        )
            throw GroupMembershipException(GlobalErrorCode.INTERNAL_SERVER_ERROR)

        return ApiResponse.of(true, HttpStatus.OK, GroupMessageConstant.MODIFY_GROUP_ROLE_SUCCESS)
    }
}