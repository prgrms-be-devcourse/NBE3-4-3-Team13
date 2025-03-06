package com.app.backend.domain.post.service.postAttachment

import com.app.backend.domain.attachment.dto.resp.FileRespDto.downloadDto
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
import lombok.RequiredArgsConstructor
import org.springframework.core.io.Resource
import org.springframework.core.io.UrlResource
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.nio.file.Paths

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
class PostAttachmentService {
    private val fileConfig: FileConfig? = null
    private val postRepository: PostRepository? = null
    private val postAttachmentRepository: PostAttachmentRepository? = null
    private val groupMembershipRepository: GroupMembershipRepository? = null

    fun downloadFile(attachmentId: Long, memberId: Long): downloadDto {
        val file = getPostAttachment(attachmentId)

        val post = getPostEntity(file.postId)

        if (post.postStatus != PostStatus.PUBLIC && getMemberShipEntity(
                post.groupId,
                memberId
            ).getStatus() != MembershipStatus.APPROVED
        ) {
            throw GroupMembershipException(GroupMembershipErrorCode.GROUP_MEMBERSHIP_NOT_FOUND)
        }

        try {
            val filePath = Paths.get(fileConfig!!.basE_DIR, file.storeFilePath)
            val resource: Resource = UrlResource(filePath.toUri())

            if (!resource.exists() || !resource.isFile) {
                throw FileException(FileErrorCode.FILE_NOT_FOUND)
            }

            return downloadDto.builder().resource(resource).attachment(file).build()
        } catch (e: Exception) {
            throw FileException(FileErrorCode.FILE_NOT_FOUND)
        }
    }

    fun getPostAttachment(attachmentId: Long): PostAttachment {
        return postAttachmentRepository!!.findById(attachmentId)
            .orElseThrow {
                FileException(
                    FileErrorCode.FILE_NOT_FOUND
                )
            }
    }

    private fun getPostEntity(postId: Long): Post {
        return postRepository!!.findByIdAndDisabled(postId, false)
            .orElseThrow { PostException(PostErrorCode.POST_NOT_FOUND) }
    }

    private fun getMemberShipEntity(groupId: Long, memberId: Long): GroupMembership {
        return groupMembershipRepository!!.findById(
            GroupMembershipId.builder().groupId(groupId).memberId(memberId).build()
        )
            .orElseThrow { GroupMembershipException(GroupMembershipErrorCode.GROUP_MEMBERSHIP_NOT_FOUND) }
    }
}
