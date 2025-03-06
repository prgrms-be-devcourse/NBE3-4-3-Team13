package com.app.backend.global.error.exception

open class DomainException(val domainErrorCode: DomainErrorCode) : RuntimeException(domainErrorCode.message)
