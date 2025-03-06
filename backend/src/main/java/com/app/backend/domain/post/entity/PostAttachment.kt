package com.app.backend.domain.post.entity

import com.app.backend.domain.attachment.entity.Attachment
import com.app.backend.domain.attachment.entity.FileType
import jakarta.persistence.*

@Entity
@Table(name = "tbl_post_attachments")
class PostAttachment(
    originalFileName: String?,
    storeFileName: String?,
    storeFilePath: String?,
    fileSize: Long?,
    contentType: String?,
    fileType: FileType?,

    @Column(name = "post_id", nullable = false)
    var postId: Long

) : Attachment(originalFileName, storeFileName, storeFilePath, fileSize, contentType, fileType) {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "attachment_id")
    var id: Long? = null

    fun delete() {
        if (!disabled) {
            deactivate()
        }
    }
}
