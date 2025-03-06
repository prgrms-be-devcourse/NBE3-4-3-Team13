package com.app.backend.global.annotation

import java.util.concurrent.TimeUnit

@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class CustomLock(
    val key: String,
    val maxWaitTime: Long = 1000L,
    val leaseTime: Long = 5000L,
    val timeUnit: TimeUnit = TimeUnit.MILLISECONDS
)
