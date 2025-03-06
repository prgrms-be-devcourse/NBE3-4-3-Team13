package com.app.backend.global.annotation

import java.util.concurrent.TimeUnit

@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FUNCTION, AnnotationTarget.PROPERTY_GETTER, AnnotationTarget.PROPERTY_SETTER)
annotation class CustomLock(
    val key: String,
    val maxWaitTime: Long = 1000L,
    val leaseTime: Long = 5000L,
    val timeUnit: TimeUnit = TimeUnit.MILLISECONDS
)
