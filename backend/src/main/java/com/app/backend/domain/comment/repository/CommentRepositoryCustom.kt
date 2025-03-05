package com.app.backend.domain.comment.repository;

import com.app.backend.domain.comment.dto.response.CommentResponse
import com.app.backend.domain.post.entity.Post
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable

interface CommentRepositoryCustom {
    fun findCommentsWithLikeCount(post: Post, memberId: Long, pageable: Pageable): Page<CommentResponse.CommentList>
}