package com.app.backend.domain.post.repository.post

import com.app.backend.domain.post.entity.Post
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Lock
import org.springframework.data.jpa.repository.Query
import jakarta.persistence.LockModeType

interface PostRepository : JpaRepository<Post, Long>, PostRepositoryCustom {

    fun findByIdAndDisabled(id: Long, disabled: Boolean): Post?

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT p FROM Post p WHERE p.id = :postId AND p.disabled = false")
    fun findByIdWithLock(postId: Long): Post?
}
