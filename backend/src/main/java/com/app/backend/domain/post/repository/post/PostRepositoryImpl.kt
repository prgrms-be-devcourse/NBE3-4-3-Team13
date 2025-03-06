package com.app.backend.domain.post.repository.post

import com.app.backend.domain.post.entity.Post
import com.app.backend.domain.post.entity.PostStatus
import com.app.backend.domain.post.entity.QPost
import com.app.backend.global.error.exception.DomainException
import com.app.backend.global.error.exception.GlobalErrorCode
import com.querydsl.core.types.Expression
import com.querydsl.core.types.Order
import com.querydsl.core.types.OrderSpecifier
import com.querydsl.core.types.dsl.BooleanExpression
import com.querydsl.core.types.dsl.Expressions
import com.querydsl.jpa.impl.JPAQueryFactory
import lombok.RequiredArgsConstructor
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Repository
import java.time.LocalDateTime
import java.util.*

@Repository
@RequiredArgsConstructor
class PostRepositoryImpl : PostRepositoryCustom {
    private val jpaQueryFactory: JPAQueryFactory? = null

    override fun findAllBySearchStatus(
        groupId: Long?,
        search: String?,
        postStatus: PostStatus,
        disabled: Boolean,
        pageable: Pageable
    ): Page<Post> {
        val post = QPost.post

        val posts = jpaQueryFactory!!.selectFrom(post)
            .where(
                searchKeywordContains(post, search),
                checkPostStatus(post, postStatus),
                post.groupId.eq(groupId),
                post.disabled.eq(disabled)
            )
            .orderBy(*getSortCondition(pageable, post))
            .offset(pageable.offset)
            .limit(pageable.pageSize.toLong())
            .fetch()

        val total = Optional.ofNullable(
            jpaQueryFactory.select(post.count())
                .from(post)
                .where(
                    searchKeywordContains(post, search),
                    checkPostStatus(post, postStatus),
                    post.groupId.eq(groupId),
                    post.disabled.eq(disabled)
                )
                .fetchOne()
        ).orElse(0L)

        return PageImpl(posts, pageable, total)
    }

    override fun findAllByUserAndSearchStatus(
        groupId: Long?,
        memberId: Long?,
        search: String?,
        postStatus: PostStatus,
        disabled: Boolean,
        pageable: Pageable
    ): Page<Post> {
        val post = QPost.post

        val posts = jpaQueryFactory!!.selectFrom(post)
            .where(
                searchKeywordContains(post, search),
                checkPostStatus(post, postStatus),
                post.groupId.eq(groupId),
                post.memberId.eq(memberId),
                post.disabled.eq(disabled)
            )
            .orderBy(*getSortCondition(pageable, post))
            .offset(pageable.offset)
            .limit(pageable.pageSize.toLong())
            .fetch()

        val total = Optional.ofNullable(
            jpaQueryFactory.select(post.count())
                .from(post)
                .where(
                    searchKeywordContains(post, search),
                    checkPostStatus(post, postStatus),
                    post.groupId.eq(groupId),
                    post.memberId.eq(memberId),
                    post.disabled.eq(disabled)
                )
                .fetchOne()
        ).orElse(0L)

        return PageImpl(posts, pageable, total)
    }

    override fun findPostsByGroupIdOrderByTodayViewsCountDesc(
        groupId: Long?,
        limit: Int,
        disabled: Boolean
    ): List<Post> {
        val post = QPost.post

        return jpaQueryFactory!!.selectFrom(post)
            .where(
                post.groupId.eq(groupId),
                post.disabled.eq(disabled),
                post.todayViewCount.gt(0L)
            )
            .orderBy(post.todayViewCount.desc())
            .limit(limit.toLong())
            .fetch()
    }

    override fun deleteAllByModifiedAtAndDisabled(lastModified: LocalDateTime, disabled: Boolean) {
        val post = QPost.post
        jpaQueryFactory
            .delete(post)
            .where(
                post.disabled.eq(disabled),
                post.modifiedAt.loe(lastModified)
            )
            .execute()
    }


    private fun searchKeywordContains(post: QPost, search: String?): BooleanExpression? {
        return if (search == null || search.isEmpty()) null else post.title.containsIgnoreCase(search)
    }

    private fun checkPostStatus(post: QPost, postStatus: PostStatus): BooleanExpression? {
        return if (postStatus == PostStatus.ALL) null else post.postStatus.eq(postStatus)
    }

    private fun isValidColumn(column: String): Boolean {
        val validColumns: List<String> = mutableListOf("title", "createdAt", "modifiedAt")
        return validColumns.contains(column)
    }

    private fun getSortCondition(pageable: Pageable, post: QPost): Array<OrderSpecifier<*>> {
        if (pageable.sort.isEmpty) {
            return arrayOf(post.createdAt.desc())
        }

        val orders: MutableList<OrderSpecifier<*>> = ArrayList()
        for (order in pageable.sort) {
            val column = order.property
            if (!isValidColumn(column)) {
                throw DomainException(GlobalErrorCode.INVALID_INPUT_VALUE)
            }

            val path: Expression<*> = Expressions.path(Comparable::class.java, post, column)
            orders.add(OrderSpecifier<Any?>(if (order.isAscending) Order.ASC else Order.DESC, path))
        }

        return orders.toTypedArray<OrderSpecifier<*>>() // 첫 번째 정렬 반환
    }
}