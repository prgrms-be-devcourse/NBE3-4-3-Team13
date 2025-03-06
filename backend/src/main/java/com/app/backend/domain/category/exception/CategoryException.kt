package com.app.backend.domain.category.exception

import com.app.backend.global.error.exception.DomainErrorCode
import com.app.backend.global.error.exception.DomainException

class CategoryException(errorCode: DomainErrorCode) : DomainException(errorCode)