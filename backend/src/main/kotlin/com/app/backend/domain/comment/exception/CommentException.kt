package com.app.backend.domain.comment.exception

import com.app.backend.global.error.exception.DomainErrorCode
import com.app.backend.global.error.exception.DomainException


class CommentException(domainErrorCode: DomainErrorCode) : DomainException(domainErrorCode)

