package com.app.backend.domain.chat.room.repository

import com.app.backend.domain.chat.room.dto.response.ChatRoomDetailResponse
import com.app.backend.domain.chat.room.dto.response.ChatRoomListResponse
import com.app.backend.domain.chat.room.entity.QChatRoom
import com.app.backend.domain.group.dto.response.GroupChatResponse
import com.app.backend.domain.group.entity.MembershipStatus
import com.app.backend.domain.group.entity.QGroup
import com.app.backend.domain.group.entity.QGroupMembership
import com.app.backend.domain.member.dto.response.MemberChatResponseDto
import com.app.backend.domain.member.entity.QMember
import com.querydsl.core.types.Projections
import com.querydsl.jpa.JPAExpressions
import com.querydsl.jpa.impl.JPAQueryFactory
import org.springframework.stereotype.Repository

@Repository
class ChatRoomRepositoryImpl(
	private val jpaQueryFactory: JPAQueryFactory
) : ChatRoomRepositoryCustom {

	override fun findAllByMemberId(memberId: Long): List<ChatRoomListResponse> {
		val chatRoom = QChatRoom.chatRoom
		val group = QGroup.group
		val groupMembership = QGroupMembership.groupMembership

		return jpaQueryFactory
			.select(Projections.constructor(ChatRoomListResponse::class.java,
				chatRoom.id,
				group.id,
				group.name,
				JPAExpressions
					.select(groupMembership.countDistinct())
					.from(groupMembership)
					.where(
						groupMembership.group.id.eq(group.id)
							.and(groupMembership.status.eq(MembershipStatus.APPROVED))
					)
			))
			.from(chatRoom)
			.join(chatRoom.group, group)
			.join(groupMembership).on(groupMembership.group.id.eq(group.id))
			.where(groupMembership.member.id.eq(memberId))
			.groupBy(chatRoom.id, group.id, group.name)
			.fetch()
	}

	override fun findByIdWithApprovedMembers(id: Long): ChatRoomDetailResponse? {
		val chatRoom = QChatRoom.chatRoom
		val group = QGroup.group
		val groupMembership = QGroupMembership.groupMembership
		val member = QMember.member

		val chatRoomDetailResponse = jpaQueryFactory
			.select(Projections.constructor(ChatRoomDetailResponse::class.java,
				chatRoom.id,
				Projections.constructor(GroupChatResponse::class.java,
					group.id,
					group.name,
					groupMembership.member.id.countDistinct()
				)
			))
			.from(chatRoom)
			.join(chatRoom.group, group)
			.leftJoin(groupMembership).on(
				groupMembership.group.eq(group)
					.and(groupMembership.status.eq(MembershipStatus.APPROVED))
			)
			.where(chatRoom.id.eq(id))
			.groupBy(chatRoom.id, group.id, group.name)
			.fetchOne()
			?: return null

		val members = jpaQueryFactory
			.select(Projections.constructor(MemberChatResponseDto::class.java,
				member.id,
				member.nickname,
				groupMembership.groupRole
			))
			.from(groupMembership)
			.join(groupMembership.member, member)
			.where(
				groupMembership.group.id.eq(chatRoomDetailResponse.group.groupId())
					.and(groupMembership.status.eq(MembershipStatus.APPROVED))
			)
			.fetch()

		return chatRoomDetailResponse.apply { addMembers(members) }
	}
}