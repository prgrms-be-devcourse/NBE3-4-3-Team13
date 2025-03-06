package com.app.backend.domain.attachment.entity

import com.app.backend.global.entity.BaseEntity
import jakarta.persistence.Column
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.MappedSuperclass
import lombok.AccessLevel
import lombok.AllArgsConstructor
import lombok.Getter
import lombok.NoArgsConstructor

@MappedSuperclass
abstract class Attachment(
    @Column(name = "original_filename", nullable = false)
    var originalFileName: String? = null,

    @Column(name = "store_filename", nullable = false)
    var storeFileName: String? = null,

    @Column(name = "store_file_path", nullable = false)
    var storeFilePath: String? = null,

    @Column(name = "file_size", nullable = false)
    var fileSize: Long? = null,

    @Column(name = "content_type", nullable = false)
    var contentType: String? = null,

    @Column(name = "file_type")
    @Enumerated(EnumType.STRING)
    var fileType: FileType? = null

) : BaseEntity()
