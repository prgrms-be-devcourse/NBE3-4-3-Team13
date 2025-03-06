package com.app.backend.domain.post.repository.post

import com.app.backend.domain.post.entity.Post
import com.app.backend.domain.post.entity.PostStatus
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import java.time.LocalDateTime

interface PostRepositoryCustom {
    fun findAllBySearchStatus(
        groupId: Long?,
        search: String?,
        postStatus: PostStatus?,
        disabled: Boolean,
        pageable: Pageable?
    ): Page<Post?>?

    fun findAllByUserAndSearchStatus(
        groupId: Long?,
        memberId: Long?,
        search: String?,
        postStatus: PostStatus?,
        disabled: Boolean,
        pageable: Pageable?
    ): Page<Post?>?

    fun findPostsByGroupIdOrderByTodayViewsCountDesc(groupId: Long?, limit: Int, disabled: Boolean): List<Post?>?

    fun deleteAllByModifiedAtAndDisabled(lastModified: LocalDateTime?, disabled: Boolean)
}
