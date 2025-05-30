package com.app.backend.domain.post.controller

import com.app.backend.domain.member.entity.MemberDetails
import com.app.backend.domain.post.dto.req.PostReqDto.*
import com.app.backend.domain.post.dto.resp.PostRespDto
import com.app.backend.domain.post.dto.resp.PostRespDto.GetPostIdDto
import com.app.backend.domain.post.entity.PostStatus
import com.app.backend.domain.post.service.post.PostService
import com.app.backend.global.annotation.CustomPageJsonSerializer
import com.app.backend.global.dto.response.ApiResponse
import jakarta.validation.Valid
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.web.PageableDefault
import org.springframework.http.HttpStatus
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile

@RestController
@RequestMapping("/api/v1/post")
class PostController(private val postService: PostService) {

    @GetMapping("/{id}")
    fun getPost(
        @PathVariable id: Long,
        @AuthenticationPrincipal memberDetails: MemberDetails
    ): ApiResponse<PostRespDto.GetPostDto> {
        postService.checkMembership(id,memberDetails.id!!)
        val post = postService.getPost(id, memberDetails.id!!)
        return ApiResponse.of(true, HttpStatus.OK, "게시글을 성공적으로 불러왔습니다", post)
    }

    @GetMapping
    @CustomPageJsonSerializer
    fun getPosts(
        @RequestParam groupId: Long,
        @RequestParam(defaultValue = "") search: String,
        @RequestParam(defaultValue = "ALL") postStatus: PostStatus,
        @PageableDefault pageable: Pageable
    ): ApiResponse<Page<PostRespDto.GetPostListDto>> {
        val posts = postService.getPostsBySearch(groupId, search, postStatus, pageable)
        return ApiResponse.of(true, HttpStatus.OK, "게시물 목록을 성공적으로 불러왔습니다", posts)
    }

    @GetMapping("/hot")
    fun getHotPosts(@RequestParam groupId: Long): ApiResponse<List<PostRespDto.GetPostListDto>> {
        val posts = postService.getTopFivePosts(groupId)
        return ApiResponse.of(true, HttpStatus.OK, "인기 게시물 목록을 성공적으로 불러왔습니다", posts)
    }

    @PostMapping
    fun savePost(
        @Valid @RequestPart("post") savePost: SavePostDto,
        @RequestPart(value = "file", required = false) files: Array<MultipartFile?>?,
        @AuthenticationPrincipal memberDetails: MemberDetails
    ): ApiResponse<GetPostIdDto> {
        val post = postService.savePost(memberDetails.id!!, savePost, files ?: emptyArray())
        return ApiResponse.of(true, HttpStatus.OK, "게시글이 성공적으로 저장되었습니다", GetPostIdDto(post.id!!))
    }

    @PatchMapping("/{id}")
    fun updatePost(
        @PathVariable id: Long,
        @Valid @RequestPart("post") modifyPost: ModifyPostDto,
        @RequestPart(value = "file", required = false) files: Array<MultipartFile?>?,
        @AuthenticationPrincipal memberDetails: MemberDetails
    ): ApiResponse<GetPostIdDto> {
        val post = postService.updatePost(memberDetails.id!!, id, modifyPost, files ?: emptyArray())
        return ApiResponse.of(true, HttpStatus.OK, "게시글이 성공적으로 수정되었습니다", GetPostIdDto(post.id!!))
    }

    @DeleteMapping("/{id}")
    fun deletePost(
        @PathVariable id: Long,
        @AuthenticationPrincipal memberDetails: MemberDetails
    ): ApiResponse<Unit> {
        postService.deletePost(memberDetails.id!!, id)
        return ApiResponse.of(true, HttpStatus.OK, "게시글이 성공적으로 삭제되었습니다")
    }

    @GetMapping("/members")
    @CustomPageJsonSerializer
    fun getMembers(
        @Valid @ModelAttribute searchPost: SearchPostDto,
        @PageableDefault pageable: Pageable,
        @AuthenticationPrincipal memberDetails: MemberDetails
    ): ApiResponse<Page<PostRespDto.GetPostListDto>> {
        val posts = postService.getPostsByUser(searchPost, pageable, memberDetails.id!!)
        return ApiResponse.of(true, HttpStatus.OK, "게시물 목록을 성공적으로 불러왔습니다", posts)
    }

    @PostMapping("/{postId}/like")
    fun createPostLike(
        @PathVariable postId: Long,
        @AuthenticationPrincipal memberDetails: MemberDetails
    ): ApiResponse<Unit> {
        postService.PostLike(postId, memberDetails.id!!)
        return ApiResponse.of(true, HttpStatus.OK, "게시글 좋아요 토글 성공")
    }

    @GetMapping("/{postId}/like")
    fun isLiked(
        @PathVariable postId: Long,
        @AuthenticationPrincipal memberDetails: MemberDetails
    ): ApiResponse<Boolean> {
        val liked = postService.isLiked(postId, memberDetails.id!!)
        return ApiResponse.of(true, HttpStatus.OK, "좋아요 여부 확인 성공", liked)
    }
}

