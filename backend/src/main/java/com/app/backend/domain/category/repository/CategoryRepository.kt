package com.app.backend.domain.category.repository

import com.app.backend.domain.category.entity.Category
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import java.util.*

interface CategoryRepository : JpaRepository<Category, Long> {

    fun existsByName(name: String): Boolean

    fun findByName(name: String): Category?

    fun findByDisabledFalse(pageable: Pageable): Page<Category>

    fun findByNameAndDisabled(name: String, disabled: Boolean): Optional<Category>
}
