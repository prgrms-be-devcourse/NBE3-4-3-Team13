package com.app.backend.global.annotation

@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class CustomPageJsonSerializer(
    val content: Boolean = true,
    val hasContent: Boolean = true,
    val totalPages: Boolean = true,
    val totalElements: Boolean = true,
    val numberOfElements: Boolean = true,
    val size: Boolean = true,
    val number: Boolean = true,
    val hasPrevious: Boolean = true,
    val hasNext: Boolean = true,
    val isFirst: Boolean = true,
    val isLast: Boolean = true,
    val sort: Boolean = true,
    val empty: Boolean = true
)
