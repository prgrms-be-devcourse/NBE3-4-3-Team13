package com.app.backend.domain.comment.repository;

import com.app.backend.domain.comment.entity.CommentLike
import jakarta.persistence.LockModeType
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Lock
import java.util.*

interface CommentLikeRepository : JpaRepository<CommentLike, Long> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    fun findByCommentIdAndMemberIdAndDisabled(
        commentId: Long,
        memberId: Long,
        disabled: Boolean
    ): Optional<CommentLike>
}