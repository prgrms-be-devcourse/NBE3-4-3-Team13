package com.app.backend.domain.post.entity

import com.app.backend.global.entity.BaseEntity
import jakarta.persistence.*
import lombok.*

@Entity
@Getter
@Builder
@Table(name = "tbl_posts")
@AllArgsConstructor
@NoArgsConstructor
class Post : BaseEntity() {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "post_id")
    var id: Long? = null

    @Setter
    @Column(nullable = false)
    var title: String? = null

    @Setter
    @Column(nullable = false)
    var content: String? = null

    @Setter
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    var postStatus: PostStatus? = null

    @Setter
    @Column(nullable = false)
    var memberId: Long? = null

    @Setter
    @Column(nullable = false)
    var nickName: String? = null

    //    Member Entity 연관관계
    //    @JoinColumn(name = "member_id")
    //    @ManyToOne(fetch = FetchType.LAZY)
    //    private Member member;
    @Setter
    @Column(nullable = false)
    var groupId: Long? = null

    //    Group Entity 연관관계
    //    @JoinColumn(name = "group_id")
    //    @ManyToOne(fetch = FetchType.LAZY)
    //    private Group group;
    @OneToMany(mappedBy = "post")
    var likes: List<PostLike> = ArrayList()

    @Column(nullable = false)
    var likeCount: Int = 0

    @Column(nullable = false)
    var todayViewCount: Long = 0L

    @Column(nullable = false)
    var totalViewCount: Long = 0L

    fun addTodayViewCount(viewCount: Long) {
        this.todayViewCount += viewCount
    }

    fun refreshViewCount() {
        totalViewCount += todayViewCount
        todayViewCount = 0L
    }

    fun delete() {
        if (!this.disabled) {
            deactivate()
        }
    }

    fun addLikeCount() {
        likeCount++
    }

    fun removeLikeCount() {
        if (this.likeCount > 0) {
            likeCount--
        }
    }

    companion object {
        fun of(
            title: String?,
            content: String?,
            postStatus: PostStatus?,
            groupId: Long?,
            memberId: Long?,
            nickName: String?
        ): Post {
            return Post(null, title, content, postStatus, memberId, nickName, groupId, ArrayList<PostLike>(), 0, 0L, 0L)
        }
    }
}
