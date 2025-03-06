package com.app.backend.domain.post.dto.resp

import com.app.backend.domain.attachment.entity.Attachment.fileSize
import com.app.backend.domain.attachment.entity.Attachment.fileType
import com.app.backend.domain.attachment.entity.FileType
import com.app.backend.domain.post.entity.PostAttachment
import lombok.AccessLevel
import lombok.AllArgsConstructor
import lombok.Builder
import lombok.Getter

object PostAttachmentRespDto {
    fun GetPostImage(
        postAttachment: PostAttachment,
        basePath: String
    ): GetPostImageDto {
        return GetPostImageDto.builder()
            .attachmentId(postAttachment.id)
            .fileName(postAttachment.originalFileName)
            .fileType(postAttachment.fileType)
            .filePath(basePath + "/" + postAttachment.storeFilePath)
            .fileSize(postAttachment.fileSize)
            .build()
    }

    fun getPostDocument(postAttachment: PostAttachment): GetPostDocumentDto {
        return GetPostDocumentDto.builder()
            .attachmentId(postAttachment.id)
            .fileName(postAttachment.originalFileName)
            .fileType(postAttachment.fileType)
            .fileSize(postAttachment.fileSize)
            .build()
    }

    @Getter
    @Builder(access = AccessLevel.PRIVATE)
    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    class GetPostImageDto {
        private val attachmentId: Long? = null
        private val fileName: String? = null
        private val fileType: FileType? = null
        private val filePath: String? = null
        private val fileSize: Long? = null
    }

    @Getter
    @Builder(access = AccessLevel.PRIVATE)
    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    class GetPostDocumentDto {
        private val attachmentId: Long? = null
        private val fileName: String? = null
        private val fileType: FileType? = null
        private val fileSize: Long? = null
    }
}

