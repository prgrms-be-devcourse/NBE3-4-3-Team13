package com.app.backend.domain.post.dto.resp

import com.app.backend.domain.attachment.entity.FileType
import com.app.backend.domain.post.entity.PostAttachment

class PostAttachmentRespDto {

    data class GetPostImageDto(
        val attachmentId: Long,
        val fileName: String,
        val fileType: FileType,
        val filePath: String,
        val fileSize: Long
    ) {
        companion object {
            @JvmStatic
            fun from(postAttachment: PostAttachment, basePath: String): GetPostImageDto {
                return GetPostImageDto(
                    attachmentId = postAttachment.id!!,
                    fileName = postAttachment.originalFileName!!,
                    fileType = postAttachment.fileType!!,
                    filePath = "$basePath/${postAttachment.storeFilePath!!}",
                    fileSize = postAttachment.fileSize!!
                )
            }
        }
    }

    data class GetPostDocumentDto(
        val attachmentId: Long,
        val fileName: String,
        val fileType: FileType,
        val fileSize: Long
    ) {
        companion object {
            @JvmStatic
            fun from(postAttachment: PostAttachment): GetPostDocumentDto {
                return GetPostDocumentDto(
                    attachmentId = postAttachment.id!!,
                    fileName = postAttachment.originalFileName!!,
                    fileType = postAttachment.fileType!!,
                    fileSize = postAttachment.fileSize!!
                )
            }
        }
    }
}

