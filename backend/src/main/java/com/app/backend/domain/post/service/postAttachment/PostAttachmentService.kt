package com.app.backend.domain.post.service.postAttachment

import com.app.backend.domain.attachment.dto.resp.FileRespDto
import com.app.backend.domain.attachment.exception.FileErrorCode
import com.app.backend.domain.attachment.exception.FileException
import com.app.backend.domain.group.entity.GroupMembership
import com.app.backend.domain.group.entity.GroupMembershipId
import com.app.backend.domain.group.entity.MembershipStatus
import com.app.backend.domain.group.exception.GroupMembershipErrorCode
import com.app.backend.domain.group.exception.GroupMembershipException
import com.app.backend.domain.group.repository.GroupMembershipRepository
import com.app.backend.domain.post.entity.Post
import com.app.backend.domain.post.entity.PostAttachment
import com.app.backend.domain.post.entity.PostStatus
import com.app.backend.domain.post.exception.PostErrorCode
import com.app.backend.domain.post.exception.PostException
import com.app.backend.domain.post.repository.post.PostRepository
import com.app.backend.domain.post.repository.postAttachment.PostAttachmentRepository
import com.app.backend.global.config.FileConfig
import org.springframework.core.io.Resource
import org.springframework.core.io.UrlResource
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.nio.file.Path
import java.nio.file.Paths

@Service
@Transactional(readOnly = true)
class PostAttachmentService(
    private val fileConfig: FileConfig,
    private val postRepository: PostRepository,
    private val postAttachmentRepository: PostAttachmentRepository,
    private val groupMembershipRepository: GroupMembershipRepository
) {

    @Throws(FileException::class)
    fun downloadFile(attachmentId: Long, memberId: Long): FileRespDto.DownloadDto {
        val file = getPostAttachment(attachmentId)
        val post = getPostEntity(file.postId)

        if (post.postStatus != PostStatus.PUBLIC && getMemberShipEntity(post.groupId, memberId).status != MembershipStatus.APPROVED) {
            throw GroupMembershipException(GroupMembershipErrorCode.GROUP_MEMBERSHIP_NOT_FOUND)
        }

        return try {
            val filePath: Path = Path.of(fileConfig.getBaseDir(), file.storeFilePath!!)
            val resource: Resource = UrlResource(filePath.toUri())

            if (!resource.exists() || !resource.isFile) {
                throw FileException(FileErrorCode.FILE_NOT_FOUND)
            }

            FileRespDto.DownloadDto(resource, file)
        } catch (e: Exception) {
            throw FileException(FileErrorCode.FILE_NOT_FOUND)
        }
    }

    fun getPostAttachment(attachmentId: Long): PostAttachment =
        postAttachmentRepository.findById(attachmentId)
            .orElseThrow { FileException(FileErrorCode.FILE_NOT_FOUND) }

    fun getPostEntity(postId: Long): Post =
        postRepository.findByIdAndDisabled(postId, false)?: throw PostException(PostErrorCode.POST_NOT_FOUND)

    fun getMemberShipEntity(groupId: Long, memberId: Long): GroupMembership =
        groupMembershipRepository.findByGroupIdAndMemberId(groupId, memberId)
            .orElseThrow { GroupMembershipException(GroupMembershipErrorCode.GROUP_MEMBERSHIP_NOT_FOUND) }
}

