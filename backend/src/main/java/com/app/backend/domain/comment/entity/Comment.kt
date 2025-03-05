package com.app.backend.domain.comment.entity

import com.app.backend.domain.member.entity.Member
import com.app.backend.domain.post.entity.Post
import com.app.backend.global.entity.BaseEntity
import jakarta.persistence.*

@Entity
@Table(name = "tbl_comments")
class Comment(
	@Id
	@Column(name = "comment_id")
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	val id: Long? = null,

	@Column(nullable = false)
	var content: String,

	@ManyToOne
	@JoinColumn(name = "post_id")
	val post: Post,

	@ManyToOne
	@JoinColumn(name = "member_id")
	val member: Member,

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "parent_id")
	var parent: Comment? = null,

	@OneToMany(mappedBy = "parent")
	val children: MutableList<Comment> = mutableListOf()
) : BaseEntity() {

	fun delete() {
		deactivate()
	}

	fun update(content: String) {
		this.content = content
	}

	fun addReply(reply: Comment) {
		children.add(reply)
		reply.parent = this
	}

	fun removeReply(reply: Comment) {
		children.remove(reply)
		reply.parent = null
	}

	fun getActiveChildren(): List<Comment> {
		return children.filter { !it.disabled }
	}
}