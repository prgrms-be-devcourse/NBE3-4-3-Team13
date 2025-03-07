package com.app.backend.domain.post.entity

import com.app.backend.global.entity.BaseEntity
import jakarta.persistence.*

@Entity
@Table(name = "tbl_posts")
class Post @JvmOverloads constructor(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "post_id")
    val id: Long? = null,

    @Column(nullable = false)
    var title: String,

    @Column(nullable = false)
    var content: String,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var postStatus: PostStatus,

    @Column(nullable = false)
    var memberId: Long,

    @Column(nullable = false)
    var nickName: String,

    @Column(nullable = false)
    var groupId: Long,

    @OneToMany(mappedBy = "post")
    var likes: MutableList<PostLike> = mutableListOf(),

    @Column(nullable = false)
    var likeCount: Int = 0,

    @Column(nullable = false)
    var todayViewCount: Long = 0L,

    @Column(nullable = false)
    var totalViewCount: Long = 0L

) : BaseEntity() {

    companion object {
        @JvmStatic
        fun of(
            title: String, content: String, postStatus: PostStatus,
            groupId: Long, memberId: Long, nickName: String
        ): Post {
            return Post(
                id = null,
                title = title,
                content = content,
                postStatus = postStatus,
                groupId = groupId,
                memberId = memberId,
                nickName = nickName,
                likes = mutableListOf(),
                likeCount = 0,
                todayViewCount = 0L,
                totalViewCount = 0L
            )
        }
    }

    fun addTodayViewCount(viewCount: Long) {
        this.todayViewCount += viewCount
    }

    fun refreshViewCount() {
        totalViewCount += todayViewCount
        todayViewCount = 0L
    }

    fun delete() {
        if (!disabled) deactivate()
    }

    fun addLikeCount() {
        likeCount++
    }

    fun removeLikeCount() {
        if (likeCount > 0) this.likeCount--
    }
}

