package com.app.backend.domain.attachment.dto.resp

import com.app.backend.domain.post.entity.PostAttachment
import org.springframework.core.io.Resource

sealed class FileRespDto {

    data class DownloadDto(
        val resource: Resource? = null,
        val attachment: PostAttachment? = null
    )
}
