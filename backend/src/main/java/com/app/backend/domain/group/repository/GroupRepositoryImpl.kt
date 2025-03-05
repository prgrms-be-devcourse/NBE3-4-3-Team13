package com.app.backend.domain.group.repository

import com.app.backend.domain.group.entity.Group
import com.app.backend.domain.group.entity.QGroup
import com.app.backend.domain.group.entity.RecruitStatus
import com.querydsl.core.types.Order
import com.querydsl.core.types.OrderSpecifier
import com.querydsl.core.types.dsl.BooleanExpression
import com.querydsl.core.types.dsl.Expressions
import com.querydsl.jpa.impl.JPAQueryFactory
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.support.PageableExecutionUtils
import org.springframework.stereotype.Repository

@Repository
class GroupRepositoryImpl(private val jpaQueryFactory: JPAQueryFactory) : GroupRepositoryCustom {
    /**
     * 검색할 지역(시/도, 시/군/구, 읍/면/동)으로 모임 목록 조회
     *
     * @param province - 시/도
     * @param city     - 시/군/구
     * @param town     - 읍/면/동
     * @param disabled - 활성화 여부(Soft Delete 상태)
     * @return 모임 목록
     */
    override fun findAllByRegion(province: String?, city: String?, town: String?, disabled: Boolean): List<Group> =
        jpaQueryFactory.selectFrom(QGroup.group)
            .where(getRegionCondition(province, city, town, QGroup.group), QGroup.group.disabled.eq(disabled))
            .fetch()

    /**
     * 검색할 지역(시/도, 시/군/구, 읍/면/동)으로 모임 페이징 목록 조회
     *
     * @param province - 시/도
     * @param city     - 시/군/구
     * @param town     - 읍/면/동
     * @param disabled - 활성화 여부(Soft Delete 상태)
     * @param pageable - 페이징 객체
     * @return 모임 페이징 목록
     */
    override fun findAllByRegion(
        province: String?,
        city: String?,
        town: String?,
        disabled: Boolean,
        pageable: Pageable
    ): Page<Group> =
        PageableExecutionUtils.getPage(
            jpaQueryFactory.selectFrom(QGroup.group)
                .where(getRegionCondition(province, city, town, QGroup.group), QGroup.group.disabled.eq(disabled))
                .orderBy(*getSortCondition(pageable, QGroup.group))
                .offset(pageable.offset)
                .limit(pageable.pageSize.toLong())
                .fetch(), pageable
        ) {
            jpaQueryFactory.select(QGroup.group.count())
                .from(QGroup.group)
                .where(getRegionCondition(province, city, town, QGroup.group), QGroup.group.disabled.eq(disabled))
                .fetchOne() ?: 0
        }

    /**
     * 모임 이름, 검색할 지역(시/도, 시/군/구, 읍/면/동)으로 모임 목록 조회
     *
     * @param name     - 모임 이름
     * @param province - 시/도
     * @param city     - 시/군/구
     * @param town     - 읍/면/동
     * @param disabled - 활성화 여부(Soft Delete 상태)
     * @return 모임 목록
     */
    override fun findAllByNameContainingAndRegion(
        name: String?,
        province: String?,
        city: String?,
        town: String?,
        disabled: Boolean
    ): List<Group> = jpaQueryFactory.selectFrom(QGroup.group)
        .where(
            QGroup.group.name.contains(name),
            getRegionCondition(province, city, town, QGroup.group),
            QGroup.group.disabled.eq(disabled)
        )
        .fetch()

    /**
     * 모임 이름, 검색할 지역(시/도, 시/군/구, 읍/면/동)으로 모임 페이징 목록 조회
     *
     * @param name     - 모임 이름
     * @param province - 시/도
     * @param city     - 시/군/구
     * @param town     - 읍/면/동
     * @param disabled - 활성화 여부(Soft Delete 상태)
     * @param pageable - 페이징 객체
     * @return 모임 페이징 목록
     */
    override fun findAllByNameContainingAndRegion(
        name: String?,
        province: String?,
        city: String?,
        town: String?,
        disabled: Boolean,
        pageable: Pageable
    ): Page<Group> =
        PageableExecutionUtils.getPage(
            jpaQueryFactory.selectFrom(QGroup.group)
                .where(
                    QGroup.group.name.contains(name),
                    getRegionCondition(province, city, town, QGroup.group),
                    QGroup.group.disabled.eq(disabled)
                )
                .orderBy(*getSortCondition(pageable, QGroup.group))
                .offset(pageable.offset)
                .limit(pageable.pageSize.toLong())
                .fetch(), pageable
        ) {
            jpaQueryFactory.select(QGroup.group.count())
                .from(QGroup.group)
                .where(
                    QGroup.group.name.contains(name),
                    getRegionCondition(province, city, town, QGroup.group),
                    QGroup.group.disabled.eq(disabled)
                )
                .fetchOne() ?: 0
        }

    /**
     * 카테고리명, 모임 이름, 검색할 지역(시/도, 시/군/구, 읍/면/동)으로 모임 목록 조회
     *
     * @param categoryName - 카테고리명
     * @param name         - 모임 이름
     * @param province     - 시/도
     * @param city         - 시/군/구
     * @param town         - 읍/면/동
     * @param disabled     - 활성화 여부(Soft Delete 상태)
     * @return 모임 목록
     */
    override fun findAllByCategoryAndNameContainingAndRegion(
        categoryName: String?,
        name: String?,
        province: String?,
        city: String?,
        town: String?,
        disabled: Boolean
    ): List<Group> = jpaQueryFactory.selectFrom(QGroup.group)
        .where(
            if (!categoryName.isNullOrBlank()) QGroup.group.category.name.eq(categoryName) else Expressions.TRUE,
            if (!name.isNullOrBlank()) QGroup.group.name.contains(name) else Expressions.TRUE,
            getRegionCondition(province, city, town, QGroup.group),
            QGroup.group.disabled.eq(disabled)
        )
        .fetch()

    /**
     * 카테고리명, 모임 이름, 검색할 지역(시/도, 시/군/구, 읍/면/동)으로 모임 페이징 목록 조회
     *
     * @param categoryName - 카테고리명
     * @param name         - 모임 이름
     * @param province     - 시/도
     * @param city         - 시/군/구
     * @param town         - 읍/면/동
     * @param disabled     - 활성화 여부(Soft Delete 상태)
     * @param pageable     - 페이징 객체
     * @return 모임 페이징 목록
     */
    override fun findAllByCategoryAndNameContainingAndRegion(
        categoryName: String?,
        name: String?,
        province: String?,
        city: String?,
        town: String?,
        disabled: Boolean,
        pageable: Pageable
    ): Page<Group> =
        PageableExecutionUtils.getPage(
            jpaQueryFactory.selectFrom(QGroup.group)
                .where(
                    if (!categoryName.isNullOrBlank()) QGroup.group.category.name.eq(categoryName) else Expressions.TRUE,
                    if (!name.isNullOrBlank()) QGroup.group.name.contains(name) else Expressions.TRUE,
                    getRegionCondition(province, city, town, QGroup.group),
                    QGroup.group.disabled.eq(disabled)
                )
                .orderBy(*getSortCondition(pageable, QGroup.group))
                .offset(pageable.offset)
                .limit(pageable.pageSize.toLong())
                .fetch(), pageable
        ) {
            jpaQueryFactory.select(QGroup.group.count())
                .from(QGroup.group)
                .where(
                    if (!categoryName.isNullOrBlank()) QGroup.group.category.name.eq(categoryName) else Expressions.TRUE,
                    if (!name.isNullOrBlank()) QGroup.group.name.contains(name) else Expressions.TRUE,
                    getRegionCondition(province, city, town, QGroup.group),
                    QGroup.group.disabled.eq(disabled)
                )
                .fetchOne() ?: 0
        }

    /**
     * 카테고리명, 모집 상태, 모임 이름, 검색할 지역(시/도, 시/군/구, 읍/면/동)으로 모임 목록 조회
     *
     * @param categoryName  - 카테고리명
     * @param recruitStatus - 모집 상태
     * @param name          - 모임 이름
     * @param province      - 시/도
     * @param city          - 시/군/구
     * @param town          - 읍/면/동
     * @param disabled      - 활성화 여부(Soft Delete 상태)
     * @return
     */
    override fun findAllByCategoryAndRecruitStatusAndNameContainingAndRegion(
        categoryName: String?,
        recruitStatus: String?,
        name: String?,
        province: String?,
        city: String?,
        town: String?,
        disabled: Boolean
    ): List<Group> = jpaQueryFactory.selectFrom(QGroup.group)
        .where(
            if (!categoryName.isNullOrBlank()) QGroup.group.category.name.eq(categoryName) else Expressions.TRUE,
            if (!recruitStatus.isNullOrBlank()) QGroup.group.recruitStatus.eq(RecruitStatus.valueOf(recruitStatus)) else Expressions.TRUE,
            if (!name.isNullOrBlank()) QGroup.group.name.contains(name) else Expressions.TRUE,
            getRegionCondition(province, city, town, QGroup.group),
            QGroup.group.disabled.eq(disabled)
        )
        .fetch()

    /**
     * 카테고리명, 모집 상태, 모임 이름, 검색할 지역(시/도, 시/군/구, 읍/면/동)으로 모임 페이징 목록 조회
     *
     * @param categoryName  - 카테고리명
     * @param recruitStatus - 모집 상태
     * @param name          - 모임 이름
     * @param province      - 시/도
     * @param city          - 시/군/구
     * @param town          - 읍/면/동
     * @param disabled      - 활성화 여부(Soft Delete 상태)
     * @param pageable      - 페이징 객체
     * @return
     */
    override fun findAllByCategoryAndRecruitStatusAndNameContainingAndRegion(
        categoryName: String?,
        recruitStatus: String?,
        name: String?,
        province: String?,
        city: String?,
        town: String?,
        disabled: Boolean,
        pageable: Pageable
    ): Page<Group> = PageableExecutionUtils.getPage(
        jpaQueryFactory.selectFrom(QGroup.group)
            .where(
                if (!categoryName.isNullOrBlank()) QGroup.group.category.name.eq(categoryName) else Expressions.TRUE,
                if (!recruitStatus.isNullOrBlank()) QGroup.group.recruitStatus.eq(RecruitStatus.valueOf(recruitStatus))
                else Expressions.TRUE,
                if (!name.isNullOrBlank()) QGroup.group.name.contains(name) else Expressions.TRUE,
                getRegionCondition(province, city, town, QGroup.group),
                QGroup.group.disabled.eq(disabled)
            ).orderBy(*getSortCondition(pageable, QGroup.group))
            .offset(pageable.offset)
            .limit(pageable.pageSize.toLong())
            .fetch(), pageable
    ) {
        jpaQueryFactory.select(QGroup.group.count())
            .from(QGroup.group)
            .where(
                if (!categoryName.isNullOrBlank()) QGroup.group.category.name.eq(categoryName) else Expressions.TRUE,
                if (!recruitStatus.isNullOrBlank()) QGroup.group.recruitStatus.eq(RecruitStatus.valueOf(recruitStatus))
                else Expressions.TRUE,
                if (!name.isNullOrBlank()) QGroup.group.name.contains(name) else Expressions.TRUE,
                getRegionCondition(province, city, town, QGroup.group),
                QGroup.group.disabled.eq(disabled)
            ).fetchOne() ?: 0
    }

    //==================== 내부 함수 ====================//

    /**
     * 검색할 지역(시/도, 시/군/구, 읍/면/동)에 따라 BooleanExpression 생성
     *
     * @param province - 시/도
     * @param city     - 시/군/구
     * @param town     - 읍/면/동
     * @param group    - QGroup
     * @return BooleanExpression
     */
    private fun getRegionCondition(province: String?, city: String?, town: String?, group: QGroup): BooleanExpression {
        var expression = Expressions.TRUE

        if (!province.isNullOrBlank()) expression = expression.and(group.province.eq(province))
        if (!city.isNullOrBlank()) expression = expression.and(group.city.eq(city))
        if (!town.isNullOrBlank()) expression = expression.and(group.town.eq(town))

        return expression
    }

    /**
     * 페이징 객체(Pageable)에 포함된 정렬 조건에 따라 Array<OrderSpecifier<*>> 생성
     *
     * @param pageable - 페이징 객체
     * @param group    - QGroup
     * @return Array<OrderSpecifier<*>>
     */
    private fun getSortCondition(pageable: Pageable, group: QGroup): Array<OrderSpecifier<*>> {
        val orderSpecifiers = mutableListOf<OrderSpecifier<*>>()

        if (!pageable.sort.isEmpty) {
            pageable.sort.forEach { order ->
                val direction = if (order.direction.isAscending) Order.ASC else Order.DESC
                val orderSpecifier = when (order.property) {
                    "name" -> OrderSpecifier(direction, group.name)
                    "recruitStatus" -> OrderSpecifier(direction, group.recruitStatus)
                    "maxRecruitStatus" -> OrderSpecifier(direction, group.maxRecruitCount)
                    "createdAt" -> OrderSpecifier(direction, group.createdAt)
                    "modifiedAt" -> OrderSpecifier(direction, group.modifiedAt)
                    else -> null
                }
                orderSpecifier?.let { orderSpecifiers.add(it) }
            }
        }

        return orderSpecifiers.toTypedArray()
    }
}