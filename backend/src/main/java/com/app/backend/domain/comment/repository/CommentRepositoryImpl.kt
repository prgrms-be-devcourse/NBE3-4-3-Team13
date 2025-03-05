package com.app.backend.domain.comment.repository

import com.app.backend.domain.comment.dto.response.CommentResponse
import com.app.backend.domain.comment.entity.QComment
import com.app.backend.domain.comment.entity.QCommentLike
import com.app.backend.domain.post.entity.Post
import com.querydsl.jpa.JPAExpressions
import com.querydsl.jpa.impl.JPAQueryFactory
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Repository
import java.util.*

@Repository
class CommentRepositoryImpl(
	private val queryFactory: JPAQueryFactory
) : CommentRepositoryCustom {

	override fun findCommentsWithLikeCount(post: Post, memberId: Long, pageable: Pageable): Page<CommentResponse.CommentList> {
		val comment = QComment.comment
		val commentLike = QCommentLike.commentLike

		val results = queryFactory
			.select(comment, commentLike.count(),
				JPAExpressions.selectOne()
					.from(commentLike)
					.where(commentLike.comment.eq(comment)
						.and(commentLike.member.id.eq(memberId))
						.and(commentLike.disabled.eq(false)))
					.exists())
			.from(comment)
			.leftJoin(comment.member).fetchJoin()
			.leftJoin(commentLike)
			.on(commentLike.comment.eq(comment)
				.and(commentLike.disabled.eq(false)))
			.where(comment.post.eq(post)
				.and(comment.disabled.eq(false))
				.and(comment.parent.isNull))
			.groupBy(comment)
			.offset(pageable.offset)
			.limit(pageable.pageSize.toLong())
			.orderBy(comment.createdAt.desc())
			.fetch()
			.map { tuple ->
				CommentResponse.CommentList.from(
					requireNotNull(tuple.get(comment)),
					Optional.ofNullable(tuple.get(1, Number::class.java))
						.map { it.toLong() }
						.orElse(0L),
					tuple.get(2, Boolean::class.java) == true
				)
			}

		val total = queryFactory
			.select(comment.count())
			.from(comment)
			.where(comment.post.eq(post)
				.and(comment.disabled.eq(false))
				.and(comment.parent.isNull))
			.fetchOne() ?: 0L

		return PageImpl(results, pageable, total)
	}
}