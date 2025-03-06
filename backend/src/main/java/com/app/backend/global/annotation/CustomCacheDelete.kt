package com.app.backend.global.annotation

import kotlin.annotation.AnnotationRetention.RUNTIME

@Target(AnnotationTarget.FUNCTION)
@Retention(RUNTIME)
annotation class CustomCacheDelete(
    val prefix: String = "global",
    val key: String = "",
    val id: String = ""
)