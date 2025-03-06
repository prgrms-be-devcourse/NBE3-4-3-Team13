package com.app.backend.domain.post.repository.post

import com.app.backend.domain.member.entity.Member
import com.app.backend.domain.post.entity.Post
import com.app.backend.domain.post.entity.PostLike
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Lock
import jakarta.persistence.LockModeType

interface PostLikeRepository : JpaRepository<PostLike, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    fun findByPostAndMember(post: Post, member: Member): PostLike?
}
