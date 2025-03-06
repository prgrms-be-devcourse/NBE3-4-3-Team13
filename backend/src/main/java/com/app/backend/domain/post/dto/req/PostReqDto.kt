package com.app.backend.domain.post.dto.req;

import com.app.backend.domain.post.entity.Post;
import com.app.backend.domain.post.entity.PostStatus;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

sealed class PostReqDto {

    data class SearchPostDto(
            @field: Positive
            val groupId: Long,

            @JsonProperty(defaultValue = "")
            val search: String,

            @JsonProperty(defaultValue = "ALL")
            val postStatus: PostStatus = PostStatus.ALL
    )

    data class ModifyPostDto(
            @field: Positive
            val groupId: Long,

            @field:NotNull
                    val title: String,

            @field:NotNull
                    val content: String,

            @field:NotNull
                    val postStatus: PostStatus,

            @JsonProperty(defaultValue = "0")
            val oldFileSize: Long,

            val remainIdList: List<Long>? = null,

            val removeIdList: List<Long>? = null

    )

    data class SavePostDto(
            @field:NotNull
            val title: String,

            @field:NotNull
                    val content: String,

            @field:NotNull
                    val postStatus: PostStatus,

            @field:Positive
                    val groupId: Long,

            ) {
        fun toEntity(memberId: Long, nickName: String): Post {
            return Post.of(this.title, this.content, this.postStatus, this.groupId, memberId, nickName);
        }
    }

}

