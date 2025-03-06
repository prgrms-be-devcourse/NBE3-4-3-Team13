package com.app.backend.domain.category.dto

import jakarta.validation.constraints.NotBlank
import org.hibernate.validator.constraints.Length

data class CategoryReqBody(
    @field:NotBlank
    @field:Length(max = 10)
    val name: String
)
