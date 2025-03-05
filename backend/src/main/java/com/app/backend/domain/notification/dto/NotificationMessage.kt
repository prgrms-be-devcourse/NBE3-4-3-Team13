package com.app.backend.domain.notification.dto

import com.fasterxml.jackson.annotation.JsonFormat
import com.fasterxml.jackson.annotation.JsonProperty
import java.io.Serializable
import java.time.LocalDateTime

data class NotificationMessage(
    @JsonProperty("id")
    val id: Long? = null,

    @JsonProperty("userId")
    val userId: String,

    @JsonProperty("title")
    val title: String,

    @JsonProperty("content")
    val content: String,

    @JsonProperty("createdAt")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    val createdAt: LocalDateTime? = null,

    @JsonProperty("isRead")
    val isRead: Boolean = false
) : Serializable