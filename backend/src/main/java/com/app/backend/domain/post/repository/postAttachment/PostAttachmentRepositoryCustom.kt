package com.app.backend.domain.post.repository.postAttachment

import com.app.backend.domain.post.entity.PostAttachment
import java.time.LocalDateTime

interface PostAttachmentRepositoryCustom {
    fun findAllByModifiedAtAndDisabled(lastModified: LocalDateTime?, disabled: Boolean): List<PostAttachment?>?

    fun deleteByIdList(idList: List<Long?>?)

    fun deleteByPostId(postId: Long?)

    fun deleteByFileIdList(fileIdList: List<Long?>?)
}
