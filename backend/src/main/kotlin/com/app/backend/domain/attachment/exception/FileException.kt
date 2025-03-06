package com.app.backend.domain.attachment.exception

import com.app.backend.global.error.exception.DomainErrorCode
import com.app.backend.global.error.exception.DomainException

class FileException(errorCode: DomainErrorCode) : DomainException(errorCode)
