package com.app.backend.domain.post.dto.resp;

import com.app.backend.domain.post.entity.Post
import com.app.backend.domain.post.entity.PostStatus
import com.app.backend.global.util.AppUtil;

sealed class PostRespDto {

    data class GetPostIdDto(
        val postId: Long
    )

    data class GetPostDto(
        val postId: Long,
        val title: String,
        val content: String,
        val postStatus: PostStatus,
        val nickName: String,
        val memberId: Long,
        val groupId: Long,
        val createdAt: String,
        val modifiedAt: String,
        val likeCount: Int,
        var liked: Boolean,
        val images: List<PostAttachmentRespDto.GetPostImageDto>? = emptyList(),
        val documents: List<PostAttachmentRespDto.GetPostDocumentDto>? = emptyList()
    ) {
        companion object {
            fun from(
                post: Post,
                memberId: Long,
                memberNickName: String,
                images: List<PostAttachmentRespDto.GetPostImageDto>? = emptyList(),
                documents: List<PostAttachmentRespDto.GetPostDocumentDto>? = emptyList(),
                isLiked: Boolean
            ): GetPostDto {
                return GetPostDto(
                    post.id!!,
                    post.title,
                    post.content,
                    post.postStatus,
                    memberNickName,
                    memberId,
                    post.groupId,
                    AppUtil.localDateTimeToString(post.createdAt),
                    AppUtil.localDateTimeToString(post.modifiedAt),
                    post.likeCount,
                    isLiked,
                    images?: emptyList(),
                    documents?: emptyList()
                )
            }
        }
    }

    data class GetPostListDto(
        val postId: Long,
        val title: String,
        val postStatus: PostStatus,
        val memberId: Long,
        val nickName: String,
        val createdAt: String,
        val todayViewCount: Long
    ) {
        companion object {
            fun from(post: Post): GetPostListDto {
                return GetPostListDto(
                    post.id!!,
                    post.title,
                    post.postStatus,
                    post.memberId,
                    post.nickName,
                    AppUtil.localDateTimeToString(post.createdAt),
                    post.todayViewCount
                )
            }
        }
    }
}