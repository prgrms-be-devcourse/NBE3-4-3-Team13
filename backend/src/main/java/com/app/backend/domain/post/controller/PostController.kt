package com.app.backend.domain.post.controller

import com.app.backend.domain.member.entity.MemberDetails
import com.app.backend.domain.post.dto.req.PostReqDto.*
import com.app.backend.domain.post.dto.resp.PostRespDto.GetPostIdDto
import com.app.backend.domain.post.entity.PostStatus
import com.app.backend.domain.post.exception.PostException
import com.app.backend.domain.post.service.post.PostService
import com.app.backend.global.annotation.CustomPageJsonSerializer
import com.app.backend.global.dto.response.ApiResponse
import com.app.backend.global.error.exception.GlobalErrorCode
import jakarta.validation.Valid
import lombok.RequiredArgsConstructor
import org.springframework.data.domain.Pageable
import org.springframework.data.web.PageableDefault
import org.springframework.http.HttpStatus
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.validation.BindingResult
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/post")
class PostController {
    private val postService: PostService? = null

    @GetMapping("/{id}")
    fun getPost(
        @PathVariable("id") postId: Long,
        @AuthenticationPrincipal memberDetails: MemberDetails
    ): ApiResponse<*> {
        val post = postService!!.getPost(postId, memberDetails.id)

        return ApiResponse.of(true, HttpStatus.OK, "게시글을 성공적으로 불러왔습니다", post)
    }

    @GetMapping
    @CustomPageJsonSerializer
    fun getPosts(
        @RequestParam groupId: Long,
        @RequestParam(defaultValue = "") search: String,
        @RequestParam(defaultValue = "ALL") postStatus: PostStatus,
        @PageableDefault pageable: Pageable
    ): ApiResponse<*> {
        val posts = postService!!.getPostsBySearch(groupId, search, postStatus, pageable)

        return ApiResponse.of(true, HttpStatus.OK, "게시물 목록을 성공적으로 불러왔습니다", posts)
    }

    @GetMapping("/hot")
    fun getHotPosts(
        @RequestParam groupId: Long
    ): ApiResponse<*> {
        val posts = postService!!.getTopFivePosts(groupId)

        return ApiResponse.of(true, HttpStatus.OK, "인기 게시물 목록을 성곡적으로 불러왔습니다", posts)
    }

    @PostMapping
    fun savePost(
        @RequestPart("post") savePost: @Valid SavePostDto,
        @RequestPart(value = "file", required = false) files: Array<MultipartFile?>,
        bindingResult: BindingResult,
        @AuthenticationPrincipal memberDetails: MemberDetails
    ): ApiResponse<*> {
        if (bindingResult.hasErrors()) {
            throw PostException(GlobalErrorCode.INVALID_INPUT_VALUE)
        }

        val post = postService!!.savePost(memberDetails.id, savePost, files)

        return ApiResponse.of(
            true, HttpStatus.OK, "게시글이 성공적으로 저장되었습니다", GetPostIdDto(
                post.id!!
            )
        )
    }

    @PatchMapping("/{id}")
    fun updatePost(
        @PathVariable("id") id: Long,
        @RequestPart("post") modifyPost: @Valid ModifyPostDto,
        @RequestPart(value = "file", required = false) files: Array<MultipartFile?>,
        bindingResult: BindingResult,
        @AuthenticationPrincipal memberDetails: MemberDetails
    ): ApiResponse<*> {
        if (bindingResult.hasErrors()) {
            throw PostException(GlobalErrorCode.INVALID_INPUT_VALUE)
        }

        val post = postService!!.updatePost(memberDetails.id, id, modifyPost, files)

        return ApiResponse.of(
            true, HttpStatus.OK, "게시글이 성공적으로 수정되었습니다", GetPostIdDto(
                post.id!!
            )
        )
    }

    @DeleteMapping("/{id}")
    fun deletePost(
        @PathVariable("id") id: Long,
        @AuthenticationPrincipal memberDetails: MemberDetails
    ): ApiResponse<*> {
        postService!!.deletePost(memberDetails.id, id)

        return ApiResponse.of<Any>(true, HttpStatus.OK, "게시글이 성공적으로 삭제되었습니다")
    }

    @GetMapping("/members")
    @CustomPageJsonSerializer
    fun getMembers(
        @ModelAttribute searchPost: @Valid SearchPostDto,
        @PageableDefault pageable: Pageable,
        bindingResult: BindingResult,
        @AuthenticationPrincipal memberDetails: MemberDetails
    ): ApiResponse<*> {
        if (bindingResult.hasErrors()) {
            throw PostException(GlobalErrorCode.INVALID_INPUT_VALUE)
        }

        val posts = postService!!.getPostsByUser(searchPost, pageable, memberDetails.id)

        return ApiResponse.of(true, HttpStatus.OK, "게시물 목록을 성공적으로 불러왔습니다", posts)
    }

    @PostMapping("/{postId}/like")
    fun createPostLike(
        @PathVariable postId: Long,
        @AuthenticationPrincipal memberDetails: MemberDetails
    ): ApiResponse<Void> {
        postService!!.PostLike(postId, memberDetails.id)
        return ApiResponse.of(true, HttpStatus.OK, "게시글 좋아요 토글 성공")
    }

    @GetMapping("/{postId}/like")
    fun isLiked(
        @PathVariable postId: Long,
        @AuthenticationPrincipal memberDetails: MemberDetails
    ): ApiResponse<Boolean> {
        val liked = postService!!.isLiked(postId, memberDetails.id)
        return ApiResponse.of(true, HttpStatus.OK, "좋아요 여부 확인 성공", liked)
    }
}
