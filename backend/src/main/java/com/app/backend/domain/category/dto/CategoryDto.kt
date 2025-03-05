package com.app.backend.domain.category.dto

import com.app.backend.domain.category.entity.Category

data class CategoryDto(
    val id: Long,
    val name: String
) {
    companion object {
        operator fun invoke(category: Category) = CategoryDto(category.id, category.name)
    }
}
