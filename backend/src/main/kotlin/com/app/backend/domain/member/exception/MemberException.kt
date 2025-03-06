package com.app.backend.domain.member.exception

import com.app.backend.global.error.exception.DomainErrorCode
import com.app.backend.global.error.exception.DomainException

class MemberException(domainErrorCode: DomainErrorCode) : DomainException(domainErrorCode)