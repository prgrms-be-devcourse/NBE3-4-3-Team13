package com.app.backend.domain.post.entity

import com.app.backend.domain.attachment.entity.Attachment
import com.app.backend.domain.attachment.entity.FileType
import jakarta.persistence.*
import lombok.AccessLevel
import lombok.AllArgsConstructor
import lombok.Getter
import lombok.NoArgsConstructor

@Entity
@Getter
@Table(name = "tbl_post_attachments")
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
class PostAttachment(
    originalFileName: String?,
    storeFileName: String?,
    storeFilePath: String?,
    fileSize: Long?,
    contentType: String?,
    fileType: FileType?,
    @field:Column(
        name = "post_id",
        nullable = false
    ) private var postId: Long
) :
    Attachment(originalFileName, storeFileName, storeFilePath, fileSize, contentType, fileType) {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "attachment_id")
    private var id: Long? = null

    fun delete() {
        if (!this.disabled) {
            deactivate()
        }
    }
}
