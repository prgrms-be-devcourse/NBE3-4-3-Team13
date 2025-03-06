package com.app.backend.domain.chat.room.exception

import com.app.backend.global.error.exception.DomainErrorCode
import com.app.backend.global.error.exception.DomainException

class ChatRoomException(domainErrorCode: DomainErrorCode) : DomainException(domainErrorCode)
