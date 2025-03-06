package com.app.backend.domain.post.exception

import com.app.backend.global.error.exception.DomainErrorCode
import com.app.backend.global.error.exception.DomainException

class PostException(errorCode: DomainErrorCode) : DomainException(errorCode)
