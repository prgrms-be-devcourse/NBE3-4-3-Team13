package com.app.backend.domain.chat.room.repository

import com.app.backend.domain.chat.room.entity.ChatRoom
import org.springframework.data.jpa.repository.JpaRepository

interface ChatRoomRepository : JpaRepository<ChatRoom, Long>, ChatRoomRepositoryCustom
