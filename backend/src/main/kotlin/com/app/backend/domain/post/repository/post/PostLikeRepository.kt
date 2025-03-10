package com.app.backend.domain.post.repository.post

import com.app.backend.domain.member.entity.Member
import com.app.backend.domain.post.entity.Post
import com.app.backend.domain.post.entity.PostLike
import jakarta.persistence.LockModeType
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Lock

interface PostLikeRepository : JpaRepository<PostLike, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    fun findByPostAndMemberAndDisabled(post: Post, member: Member, disabled: Boolean): PostLike?
}
