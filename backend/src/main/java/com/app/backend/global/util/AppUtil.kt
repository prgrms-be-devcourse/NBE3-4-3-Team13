package com.app.backend.global.util

import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*

object AppUtil {
    private val DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
    private val DATE_FORMAT = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")

    fun localDateTimeToString(localDateTime: LocalDateTime) = DATE_TIME_FORMATTER.format(localDateTime)
    fun DateToString(date: Date) = DATE_FORMAT.format(date)
}
