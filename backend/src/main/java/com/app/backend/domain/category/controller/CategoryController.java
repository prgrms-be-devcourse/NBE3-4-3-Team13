package com.app.backend.domain.category.controller;


import com.app.backend.domain.category.dto.CategoryDto;
import com.app.backend.domain.category.dto.CategoryPageDto;
import com.app.backend.domain.category.dto.CategoryReqBody;
import com.app.backend.domain.category.entity.Category;
import com.app.backend.domain.category.service.CategoryService;
import com.app.backend.global.dto.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/admin/categories")
public class CategoryController {

	private final CategoryService categoryService;

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	public ApiResponse<CategoryDto> createCategory(
		@RequestBody CategoryReqBody request
	) {
		Category category = categoryService.create(request.name);

		CategoryDto categoryDto = CategoryDto.from(category);

		return ApiResponse.of(
			true,
			HttpStatus.CREATED,
			"%s 카테고리가 생성되었습니다.".formatted(category.getName()),
			categoryDto
		);
	}

	@GetMapping
	public ApiResponse<CategoryPageDto> getCategories(
		@PageableDefault(page = 0, size = 10) Pageable pageable
	) {
		CategoryPageDto categoryPageDto = categoryService.getCategories(pageable);

		return ApiResponse.of(
			true,
			HttpStatus.OK,
			"카테고리 목록 조회",
			categoryPageDto
		);
	}

	@PatchMapping("/{id}")
	public ApiResponse<CategoryDto> modifyCategory(
		@PathVariable Long id,
		@RequestBody CategoryReqBody modifyRequest
	) {

		Category category = categoryService.findById(id);

		categoryService.modify(category, modifyRequest.name);

		CategoryDto categoryDto = CategoryDto.from(category);

		return ApiResponse.of(
			true,
			HttpStatus.OK,
			"%d번 카테고리가 수정되었습니다.".formatted(category.getId()),
			categoryDto
		);
	}

	@DeleteMapping("/{id}")
	public ApiResponse<Void> deleteCategory(
		@PathVariable Long id
	) {
		categoryService.softDelete(id);

		return ApiResponse.of(
			true,
			HttpStatus.OK,
			"%d번 카테고리가 삭제되었습니다.".formatted(id)
		);
	}

}
