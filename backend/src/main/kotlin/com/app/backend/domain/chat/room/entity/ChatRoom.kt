package com.app.backend.domain.chat.room.entity

import com.app.backend.domain.group.entity.Group
import com.app.backend.global.entity.BaseEntity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table

@Entity
@Table(name = "tbl_chat_rooms")
data class ChatRoom(
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "chat_room_id")
	val id: Long? = null,

	@ManyToOne(fetch = FetchType.LAZY)
	val group: Group
) : BaseEntity() {

	constructor(group: Group) : this(id = null, group = group)

	/**
	 * 채팅방 삭제
	 */
	fun delete() {
		if (!this.disabled) {
			deactivate()
		}
	}
}
