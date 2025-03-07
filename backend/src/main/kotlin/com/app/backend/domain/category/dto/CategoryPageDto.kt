package com.app.backend.domain.category.dto

data class CategoryPageDto(
    val categories: List<CategoryDto>,
    val currentPage: Int,
    val totalPages: Int,
    val totalItems: Long,
    val pageSize: Int
)
