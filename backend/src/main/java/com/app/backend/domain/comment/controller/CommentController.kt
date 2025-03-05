package com.app.backend.domain.comment.controller

import com.app.backend.domain.comment.dto.request.CommentCreateRequest
import com.app.backend.domain.comment.dto.response.CommentResponse.*
import com.app.backend.domain.comment.service.CommentService
import com.app.backend.domain.member.entity.MemberDetails
import com.app.backend.global.annotation.CustomPageJsonSerializer
import com.app.backend.global.dto.response.ApiResponse
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.data.web.PageableDefault
import org.springframework.http.HttpStatus
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/v1/comment")
class CommentController(
	private val commentService: CommentService
) {
	// 게시물에 대한 댓글 작성
	@PostMapping("/{id}")
	@ResponseStatus(HttpStatus.CREATED)
	fun createComment(
		@PathVariable(name = "id") postId: Long,
		@RequestBody req: CommentCreateRequest,
		@AuthenticationPrincipal memberDetails: MemberDetails
	): ApiResponse<CommentDto> {
		val response = commentService.createComment(postId, memberDetails.id!!, req)

		return ApiResponse.of(
			true,
			HttpStatus.CREATED,
			"${response.id}번 댓글이 작성되었습니다.",
			response
		)
	}

	// 댓글 삭제
	@DeleteMapping("/{id}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	fun deleteComment(
		@PathVariable(name = "id") commentId: Long,
		@AuthenticationPrincipal memberDetails: MemberDetails
	): ApiResponse<Void> {
		commentService.deleteComment(commentId, memberDetails.id!!)

		return ApiResponse.of(
			true,
			HttpStatus.NO_CONTENT,
			"${commentId}번 댓글이 삭제되었습니다."
		)
	}

	// 댓글 수정
	@PatchMapping("/{id}")
	fun updateComment(
		@PathVariable(name = "id") commentId: Long,
		@RequestBody req: CommentCreateRequest,
		@AuthenticationPrincipal memberDetails: MemberDetails
	): ApiResponse<CommentDto> {
		val response = commentService.updateComment(commentId, memberDetails.id!!, req)

		return ApiResponse.of(
			true,
			HttpStatus.OK,
			"${commentId}번 댓글이 수정되었습니다.",
			response
		)
	}

	// 게시물에 대한 댓글 조회 페이징
	@GetMapping("/{id}")
	@CustomPageJsonSerializer(
		empty = false,
		numberOfElements = false,
		size = false,
		number = false,
		hasPrevious = false,
		isFirst = false
	)
	fun getComments(
		@PathVariable(name = "id") postId: Long,
		@PageableDefault(size = 10, sort = ["created_at"], direction = Sort.Direction.DESC) pageable: Pageable,
		@AuthenticationPrincipal memberDetails: MemberDetails
	): ApiResponse<Page<CommentList>> {
		val response = commentService.getComments(postId, memberDetails.id!!, pageable)

		return ApiResponse.of(
			true,
			HttpStatus.OK,
			"댓글이 조회되었습니다.",
			response
		)
	}

	// 대댓글 작성
	@PostMapping("/{id}/reply")
	@ResponseStatus(HttpStatus.CREATED)
	fun createReply(
		@PathVariable(name = "id") commentId: Long,
		@RequestBody req: CommentCreateRequest,
		@AuthenticationPrincipal memberDetails: MemberDetails
	): ApiResponse<ReplyDto> {
		val response = commentService.createReply(commentId, memberDetails.id!!, req)

		return ApiResponse.of(
			true,
			HttpStatus.CREATED,
			"${commentId}번 댓글에 대한 답글이 작성되었습니다.",
			response
		)
	}

	// 대댓글 수정
	@PatchMapping("/{id}/reply")
	fun updateReply(
		@PathVariable(name = "id") replyId: Long,
		@RequestBody req: CommentCreateRequest,
		@AuthenticationPrincipal memberDetails: MemberDetails
	): ApiResponse<ReplyDto> {
		val response = commentService.updateReply(replyId, memberDetails.id!!, req)

		return ApiResponse.of(
			true,
			HttpStatus.OK,
			"${replyId}번 답글이 수정되었습니다.",
			response
		)
	}

	// 대댓글 삭제
	@DeleteMapping("/{id}/reply")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	fun deleteReply(
		@PathVariable(name = "id") replyId: Long,
		@AuthenticationPrincipal memberDetails: MemberDetails
	): ApiResponse<Void> {
		commentService.deleteReply(replyId, memberDetails.id!!)

		return ApiResponse.of(
			true,
			HttpStatus.NO_CONTENT,
			"${replyId}번 답글이 삭제되었습니다."
		)
	}

	// 대댓글 조회
	@GetMapping("/{id}/reply")
	@CustomPageJsonSerializer(
		empty = false,
		hasContent = false,
		numberOfElements = false,
		size = false,
		number = false,
		hasPrevious = false,
		isFirst = false
	)
	fun getReplies(
		@PathVariable(name = "id") commentId: Long,
		@PageableDefault(size = 10, sort = ["created_at"], direction = Sort.Direction.DESC) pageable: Pageable
	): ApiResponse<Page<ReplyList>> {
		val response = commentService.getReplies(commentId, pageable)

		return ApiResponse.of(
			true,
			HttpStatus.OK,
			"답글이 조회되었습니다.",
			response
		)
	}

	// 댓글 좋아요
	@PostMapping("/{id}/like")
	fun createCommentLike(
		@PathVariable(name = "id") commentId: Long,
		@AuthenticationPrincipal memberDetails: MemberDetails
	): ApiResponse<Void> {
		commentService.commentLike(commentId, memberDetails.id!!)

		return ApiResponse.of(
			true,
			HttpStatus.OK,
			"댓글 좋아요가 추가되었습니다."
		)
	}
}