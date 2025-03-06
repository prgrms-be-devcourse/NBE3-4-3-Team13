package com.app.backend.domain.comment.service

import com.app.backend.domain.comment.dto.request.CommentCreateRequest
import com.app.backend.domain.comment.dto.response.CommentResponse
import com.app.backend.domain.comment.entity.Comment
import com.app.backend.domain.comment.entity.CommentLike
import com.app.backend.domain.comment.exception.CommentErrorCode
import com.app.backend.domain.comment.exception.CommentException
import com.app.backend.domain.comment.repository.CommentLikeRepository
import com.app.backend.domain.comment.repository.CommentRepository
import com.app.backend.domain.member.repository.MemberRepository
import com.app.backend.domain.post.entity.Post
import com.app.backend.domain.post.exception.PostErrorCode
import com.app.backend.domain.post.exception.PostException
import com.app.backend.domain.post.repository.post.PostRepository
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional(readOnly = true)
class CommentService(
    private val commentRepository: CommentRepository,
    private val postRepository: PostRepository,
    private val memberRepository: MemberRepository,
    private val commentLikeRepository: CommentLikeRepository
) {
    // 댓글 조회
    private fun getCommentValidate(id: Long): Comment {
        return commentRepository.findByIdAndDisabled(id, false)
            .orElseThrow { CommentException(CommentErrorCode.COMMENT_NOT_FOUND) }
    }

    // 게시물 조회
    private fun getPostValidate(postId: Long): Post {
        return postRepository.findByIdAndDisabled(postId, false)
            ?: throw PostException(PostErrorCode.POST_NOT_FOUND)
    }

    // 댓글 작성자만 수정과 삭제 가능
    private fun validateAuthor(comment: Comment, memberId: Long) {
        if (comment.member.id != memberId) {
            throw CommentException(CommentErrorCode.COMMENT_ACCESS_DENIED)
        }
    }

    // 댓글 내용이 없으면 댓글 작성 실패
    private fun validateCommentContent(content: String?) {
        if (content.isNullOrBlank()) {
            throw CommentException(CommentErrorCode.COMMENT_INVALID_CONTENT)
        }
    }

    // 댓글 작성
    @Transactional
    fun createComment(postId: Long, memberId: Long, req: CommentCreateRequest): CommentResponse.CommentDto {
        validateCommentContent(req.content)

        val post = getPostValidate(postId)
        val member = memberRepository.findByIdAndDisabled(memberId, false)
            .orElseThrow { IllegalArgumentException("사용자를 찾을 수 없습니다.") }

        val comment = Comment(
            content = req.content!!,
            post = post,
            member = member
        )

        commentRepository.save(comment)
        return CommentResponse.CommentDto.from(comment)
    }

    // 댓글 삭제
    @Transactional
    fun deleteComment(commentId: Long, memberId: Long) {
        val comment = getCommentValidate(commentId)
        validateAuthor(comment, memberId)
        comment.delete()
    }

    // 댓글 수정
    @Transactional
    fun updateComment(commentId: Long, memberId: Long, req: CommentCreateRequest): CommentResponse.CommentDto {
        val comment = getCommentValidate(commentId)
        validateCommentContent(req.content)
        validateAuthor(comment, memberId)
        comment.update(req.content!!)
        return CommentResponse.CommentDto.from(comment)
    }

    // 댓글 조회 (좋아요 포함)
    fun getComments(postId: Long, memberId: Long, pageable: Pageable): Page<CommentResponse.CommentList> {
        val post = getPostValidate(postId)
        return commentRepository.findCommentsWithLikeCount(post, memberId, pageable)
    }

    // 대댓글 작성
    @Transactional
    fun createReply(commentId: Long, memberId: Long, req: CommentCreateRequest): CommentResponse.ReplyDto {
        val parentComment = getCommentValidate(commentId)
        val member = memberRepository.findByIdAndDisabled(memberId, false)
            .orElseThrow { IllegalArgumentException("사용자를 찾을 수 없습니다.") }

        validateCommentContent(req.content)

        val reply = Comment(
            content = req.content!!,
            post = parentComment.post,
            member = member,
            parent = parentComment
        )

        val savedReply = commentRepository.save(reply)
        parentComment.addReply(savedReply)
        return CommentResponse.ReplyDto.from(savedReply)
    }

    // 대댓글 수정
    @Transactional
    fun updateReply(replyId: Long, id: Long, req: CommentCreateRequest): CommentResponse.ReplyDto {
        val reply = getCommentValidate(replyId)
        validateCommentContent(req.content)
        validateAuthor(reply, id)
        reply.update(req.content!!)
        return CommentResponse.ReplyDto.from(reply)
    }

    // 대댓글 삭제
    @Transactional
    fun deleteReply(replyId: Long, id: Long) {
        val reply = getCommentValidate(replyId)
        validateAuthor(reply, id)
        reply.parent?.removeReply(reply)
        reply.delete()
    }

    // 대댓글 조회
    fun getReplies(commentId: Long, pageable: Pageable): Page<CommentResponse.ReplyList> {
        val comment = getCommentValidate(commentId)
        val replies = commentRepository.findByParentAndDisabled(comment, false, pageable)
        return replies.map { CommentResponse.ReplyList.from(it) }
    }

    // 댓글 좋아요
    @Transactional
    fun commentLike(commentId: Long, memberId: Long) {
        val comment = getCommentValidate(commentId)
        val member = memberRepository.findByIdAndDisabled(memberId, false)
            .orElseThrow { IllegalArgumentException("사용자를 찾을 수 없습니다.") }

        val isLike = commentLikeRepository.findByCommentIdAndMemberIdAndDisabled(commentId, memberId, false)

        if (isLike.isPresent) {
            isLike.get().delete() // 좋아요가 있으면 삭제
        } else {
            val commentLike = CommentLike(
                comment = comment,
                member = member
            )
            commentLikeRepository.save(commentLike)
        }
    }
}