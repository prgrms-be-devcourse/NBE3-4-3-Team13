package com.app.backend.domain.group.controller

import com.app.backend.domain.group.service.GroupLikeService
import com.app.backend.domain.member.entity.MemberDetails
import com.app.backend.global.dto.response.ApiResponse
import org.springframework.http.HttpStatus
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/v1/groups/{groupId}/like")
class GroupLikeController(private val groupLikeService: GroupLikeService) {
    /** 현재 사용자가 해당 그룹을 좋아요헀는지 여부 확인 */
    @GetMapping
    fun isLiked(
        @PathVariable groupId: Long,
        @AuthenticationPrincipal memberDetails: MemberDetails
    ): ApiResponse<Boolean> {
        val liked = groupLikeService.isLiked(groupId, memberDetails.id!!)
        return ApiResponse.of(true, HttpStatus.OK, "좋아요 여부 확인 성공")
    }

    /** 그룹 좋아요 추가 */
    @PostMapping
    fun likeGroup(
        @PathVariable groupId: Long,
        @AuthenticationPrincipal memberDetails: MemberDetails
    ): ApiResponse<Unit> {
        groupLikeService.likeGroup(groupId, memberDetails.id!!)
        return ApiResponse.of(true, HttpStatus.OK, "좋아요 성공")
    }

    /** 그룹 좋아요 취소 */
    @DeleteMapping
    fun unlikeGroup(
        @PathVariable groupId: Long,
        @AuthenticationPrincipal memberDetails: MemberDetails
    ): ApiResponse<Unit> {
        groupLikeService.unlikeGroup(groupId, memberDetails.id!!)
        return ApiResponse.of(true, HttpStatus.OK, "좋아요 취소 성공")
    }
}