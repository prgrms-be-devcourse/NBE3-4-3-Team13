package com.app.backend.global.annotation

import java.util.concurrent.TimeUnit

@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class CustomCache(
    val prefix: String = "global",
    val key: String = "",
    val id: String = "",
    val ttl: Long = 5,
    val ttlUnit: TimeUnit = TimeUnit.MINUTES,
    val viewCount: Boolean = false,
    val viewCountTtl: Long = 5,
    val viewCountTtlUnit: TimeUnit = TimeUnit.MINUTES,
    val history: Boolean = false
)
