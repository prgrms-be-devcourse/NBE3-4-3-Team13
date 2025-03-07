package com.app.backend.domain.category.controller

import com.app.backend.domain.category.dto.CategoryDto
import com.app.backend.domain.category.dto.CategoryPageDto
import com.app.backend.domain.category.dto.CategoryReqBody
import com.app.backend.domain.category.service.CategoryService
import com.app.backend.global.dto.response.ApiResponse
import org.springframework.data.domain.Pageable
import org.springframework.data.web.PageableDefault
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*


@RestController
@RequestMapping("/api/v1/admin/categories")
class CategoryController(
    private val categoryService: CategoryService
) {

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun createCategory(
        @RequestBody request: CategoryReqBody
    ): ApiResponse<CategoryDto> {
        val category = categoryService.create(request.name)

        val categoryDto: CategoryDto = CategoryDto(category)

        return ApiResponse.of(
            true,
            HttpStatus.CREATED,
            "%s 카테고리가 생성되었습니다.".formatted(category.name),
            categoryDto
        )
    }

    @GetMapping
    fun getCategories(
        @PageableDefault(page = 0, size = 10) pageable: Pageable
    ): ApiResponse<CategoryPageDto> {
        val categoryPageDto = categoryService.getCategories(pageable)

        return ApiResponse.of(
            true,
            HttpStatus.OK,
            "카테고리 목록 조회",
            categoryPageDto
        )
    }

    @PatchMapping("/{id}")
    fun modifyCategory(
        @PathVariable id: Long,
        @RequestBody modifyRequest: CategoryReqBody
    ): ApiResponse<CategoryDto> {
        val category = categoryService.findById(id)

        categoryService.modify(category, modifyRequest.name)

        val categoryDto: CategoryDto = CategoryDto(category)

        return ApiResponse.of(
            true,
            HttpStatus.OK,
            "%d번 카테고리가 수정되었습니다.".formatted(category.id),
            categoryDto
        )
    }

    @DeleteMapping("/{id}")
    fun deleteCategory(
        @PathVariable id: Long
    ): ApiResponse<Void> {
        categoryService.softDelete(id)

        return ApiResponse.of(
            true,
            HttpStatus.OK,
            "%d번 카테고리가 삭제되었습니다.".formatted(id)
        )
    }
}
