package com.app.backend.domain.comment.repository;

import com.app.backend.domain.comment.entity.Comment
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import java.util.*

interface CommentRepository : JpaRepository<Comment, Long>, CommentRepositoryCustom {
    fun findByIdAndDisabled(id: Long, disabled: Boolean): Optional<Comment>

    fun findByParentAndDisabled(comment: Comment, disabled: Boolean, pageable: Pageable): Page<Comment>
}

