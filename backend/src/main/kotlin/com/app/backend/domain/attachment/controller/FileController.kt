package com.app.backend.domain.attachment.controller

import com.app.backend.domain.member.entity.MemberDetails
import com.app.backend.domain.post.service.postAttachment.PostAttachmentService
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/download")
class FileController(
    private val postAttachmentService: PostAttachmentService
) {

    @GetMapping("/post/{id}")
    fun downloadFile(
        @PathVariable id: Long,
        @AuthenticationPrincipal memberDetails: MemberDetails
    ): ResponseEntity<*> {
        val downloadFile = postAttachmentService.downloadFile(id, memberDetails.id!!)
        val contentType = downloadFile.attachment?.contentType ?: MediaType.APPLICATION_OCTET_STREAM_VALUE // ✅ Elvis 연산자 사용

        return ResponseEntity.ok()
            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"${downloadFile.attachment?.originalFileName}\"") // ✅ String Template 사용
            .contentType(MediaType.parseMediaType(contentType))
            .body(downloadFile.resource)
    }
}
