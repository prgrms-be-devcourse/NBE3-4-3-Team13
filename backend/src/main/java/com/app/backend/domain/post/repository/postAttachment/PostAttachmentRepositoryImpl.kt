package com.app.backend.domain.post.repository.postAttachment

import com.app.backend.domain.post.entity.PostAttachment
import com.app.backend.domain.post.entity.QPostAttachment
import com.querydsl.jpa.impl.JPAQueryFactory
import org.springframework.stereotype.Repository
import java.time.LocalDateTime

@Repository
class PostAttachmentRepositoryImpl(
    private val jpaQueryFactory: JPAQueryFactory
) : PostAttachmentRepositoryCustom {

    override fun findAllByModifiedAtAndDisabled(lastModified: LocalDateTime, disabled: Boolean): List<PostAttachment> {
        val postAttachment = QPostAttachment.postAttachment
        return jpaQueryFactory
            .selectFrom(postAttachment)
            .where(
                postAttachment.disabled.eq(disabled),
                postAttachment.modifiedAt.loe(lastModified)
            )
            .fetch()
    }

    override fun deleteByIdList(idList: List<Long>) {
        val postAttachment = QPostAttachment.postAttachment
        jpaQueryFactory
            .update(postAttachment)
            .set(postAttachment.disabled, true)
            .where(
                postAttachment.id.`in`(idList)
                    .and(postAttachment.disabled.eq(false))
            )
            .execute()
    }

    override fun deleteByPostId(postId: Long) {
        val postAttachment = QPostAttachment.postAttachment
        jpaQueryFactory
            .update(postAttachment)
            .set(postAttachment.disabled, true)
            .where(
                postAttachment.postId.eq(postId)
                    .and(postAttachment.disabled.eq(false))
            )
            .execute()
    }

    override fun deleteByFileIdList(fileIdList: List<Long>) {
        val postAttachment = QPostAttachment.postAttachment
        jpaQueryFactory
            .delete(postAttachment)
            .where(postAttachment.postId.`in`(fileIdList))
            .execute()
    }
}

