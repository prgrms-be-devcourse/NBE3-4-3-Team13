package com.app.backend.domain.meetingApplication.dto

data class MeetingApplicationReqBody(
    val context: String
) {
    init {
        require(context.isNotBlank()) { "Context cannot be blank" }
    }
}
