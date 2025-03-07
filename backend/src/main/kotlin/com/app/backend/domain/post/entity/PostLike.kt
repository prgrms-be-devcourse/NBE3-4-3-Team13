package com.app.backend.domain.post.entity

import com.app.backend.domain.member.entity.Member
import com.app.backend.global.entity.BaseEntity
import jakarta.persistence.*

@Entity
@Table(name = "tbl_post_likes")
class PostLike(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "post_like_id")
    val id: Long? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    val member: Member,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id", nullable = false)
    val post: Post
) : BaseEntity() {
    fun delete() {
        deactivate()
    }
}
