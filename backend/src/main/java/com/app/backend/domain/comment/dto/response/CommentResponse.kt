package com.app.backend.domain.comment.dto.response

import com.app.backend.domain.comment.entity.Comment
import java.time.LocalDateTime

class CommentResponse {

    data class CommentList(
        val id: Long?,
        val content: String,
        val memberId: Long?,
        val nickname: String?,
        val createdAt: LocalDateTime,
        val replyCount: Int,
        val likeCount: Long,
        val liked: Boolean
    ) {
        companion object {
            fun from(comment: Comment, likeCount: Long, liked: Boolean): CommentList {
                return CommentList(
                    id = comment.id,
                    content = comment.content,
                    memberId = comment.member.id,
                    nickname = comment.member.nickname,
                    createdAt = comment.createdAt,
                    replyCount = comment.getActiveChildren().size,
                    likeCount = likeCount,
                    liked = liked
                )
            }
        }
    }

    data class ReplyList(
        val id: Long?,
        val content: String,
        val postId: Long?,
        val memberId: Long?,
        val nickname: String?,
        val parentId: Long?,
        val createdAt: LocalDateTime,
        val modifiedAt: LocalDateTime
    ) {
        companion object {
            fun from(reply: Comment): ReplyList {
                return ReplyList(
                    id = reply.id,
                    content = reply.content,
                    postId = reply.post.id,
                    memberId = reply.member.id,
                    nickname = reply.member.nickname,
                    parentId = reply.parent?.id,
                    createdAt = reply.createdAt,
                    modifiedAt = reply.modifiedAt
                )
            }
        }
    }

    data class CommentDto(
        val id: Long?,
        val content: String,
        val postId: Long?,
        val memberId: Long?,
        val nickname: String?,
        val createdAt: LocalDateTime,
        val modifiedAt: LocalDateTime,
        val replyCount: Int
    ) {
        companion object {
            fun from(comment: Comment): CommentDto {
                return CommentDto(
                    id = comment.id,
                    content = comment.content,
                    postId = comment.post.id,
                    memberId = comment.member.id,
                    nickname = comment.member.nickname,
                    createdAt = comment.createdAt,
                    modifiedAt = comment.modifiedAt,
                    replyCount = comment.getActiveChildren().size
                )
            }
        }
    }

    data class ReplyDto(
        val id: Long?,
        val content: String,
        val memberId: Long?,
        val nickname: String?,
        val parentId: Long?,
        val createdAt: LocalDateTime,
        val modifiedAt: LocalDateTime
    ) {
        companion object {
            fun from(reply: Comment): ReplyDto {
                return ReplyDto(
                    id = reply.id,
                    content = reply.content,
                    memberId = reply.member.id,
                    nickname = reply.member.nickname,
                    parentId = reply.parent?.id,
                    createdAt = reply.createdAt,
                    modifiedAt = reply.modifiedAt
                )
            }
        }
    }
}