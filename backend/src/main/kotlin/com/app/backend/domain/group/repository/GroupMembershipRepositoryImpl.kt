package com.app.backend.domain.group.repository

import com.querydsl.jpa.impl.JPAQueryFactory
import org.springframework.stereotype.Repository

@Repository
class GroupMembershipRepositoryImpl(private val jpaQueryFactory: JPAQueryFactory) : GroupMembershipRepositoryCustom
