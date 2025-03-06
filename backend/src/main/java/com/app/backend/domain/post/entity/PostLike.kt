package com.app.backend.domain.post.entity

import com.app.backend.domain.member.entity.Member
import com.app.backend.global.entity.BaseEntity
import jakarta.persistence.*
import lombok.AllArgsConstructor
import lombok.Builder
import lombok.Getter
import lombok.NoArgsConstructor

@Entity
@Table(name = "tbl_post_likes")
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
class PostLike : BaseEntity() {
    @Id
    @Column(name = "post_like_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private var id: Long? = null

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private var member: Member? = null

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id", nullable = false)
    private var post: Post? = null

    fun delete() {
        this.deactivate()
    }
}
