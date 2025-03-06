package com.app.backend.domain.post.repository.postAttachment

import com.app.backend.domain.attachment.entity.FileType
import com.app.backend.domain.post.entity.PostAttachment
import org.springframework.data.jpa.repository.JpaRepository

interface PostAttachmentRepository : JpaRepository<PostAttachment, Long>, PostAttachmentRepositoryCustom {

    fun findByPostIdAndDisabled(postId: Long, disabled: Boolean): List<PostAttachment>

    fun findByPostIdAndFileTypeAndDisabledOrderByCreatedAtDesc(postId: Long, fileType: FileType, disabled: Boolean): List<PostAttachment>

    fun findByPostIdAndFileTypeOrderByCreatedAtDesc(postId: Long, fileType: FileType): List<PostAttachment>
}
