package com.app.backend.domain.group.exception

import com.app.backend.global.error.exception.DomainErrorCode
import com.app.backend.global.error.exception.DomainException

class GroupLikeException(domainErrorCode: DomainErrorCode) : DomainException(domainErrorCode)