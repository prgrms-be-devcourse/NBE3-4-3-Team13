package com.app.backend.domain.meetingApplication.exception

import com.app.backend.global.error.exception.DomainErrorCode
import com.app.backend.global.error.exception.DomainException

class MeetingApplicationException(errorCode: DomainErrorCode) : DomainException(errorCode)