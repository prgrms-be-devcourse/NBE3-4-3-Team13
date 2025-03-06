package com.app.backend.domain.category.service

import com.app.backend.domain.category.dto.CategoryDto
import com.app.backend.domain.category.dto.CategoryPageDto
import com.app.backend.domain.category.entity.Category
import com.app.backend.domain.category.exception.CategoryErrorCode
import com.app.backend.domain.category.exception.CategoryException
import com.app.backend.domain.category.repository.CategoryRepository
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional(readOnly = true)
open class CategoryService(
    private val categoryRepository: CategoryRepository
) {

    @Transactional
    fun create(name: String): Category {
        validateCategoryName(name) // 입력값 검증
        val category = Category(name)
        return categoryRepository.save(category)
    }

    // 카테고리 목록 페이지로 조회
    fun getCategories(pageable: Pageable): CategoryPageDto {
        val categoryPage: Page<Category> = categoryRepository.findByDisabledFalse(pageable)

        val categories = categoryPage.content.map { CategoryDto(it) }

        return CategoryPageDto(
            categories = categories,
            currentPage = categoryPage.number + 1,
            totalPages = categoryPage.totalPages,
            totalItems = categoryPage.totalElements,
            pageSize = categoryPage.size
        )
    }

    // 검증 메서드
    private fun validateCategoryName(name: String) {
        when {
            name.isBlank() -> throw CategoryException(CategoryErrorCode.CATEGORY_NAME_REQUIRED)
            name.length > 10 -> throw CategoryException(CategoryErrorCode.CATEGORY_NAME_TOO_LONG)
            categoryRepository.existsByName(name) -> throw CategoryException(CategoryErrorCode.CATEGORY_NAME_DUPLICATE)
        }
    }

    fun findById(id: Long): Category {
        return categoryRepository.findById(id)
            .orElseThrow { CategoryException(CategoryErrorCode.CATEGORY_NOT_FOUND) }
    }

    @Transactional
    fun modify(category: Category, newName: String) {
        validateCategoryName(newName) // 입력값 검증
        category.modifyName(newName)
    }

    @Transactional
    fun softDelete(id: Long) {
        val category = categoryRepository.findById(id)
            .orElseThrow { CategoryException(CategoryErrorCode.CATEGORY_NOT_FOUND) }

        category.softDelete()
        categoryRepository.save(category)
    }
}
