package com.app.backend.domain.group.repository

import com.app.backend.domain.group.entity.Group
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable

interface GroupRepositoryCustom {
    fun findAllByRegion(
        province: String?,
        city: String?,
        town: String?,
        disabled: Boolean
    ): List<Group>

    fun findAllByRegion(
        province: String?,
        city: String?,
        town: String?,
        disabled: Boolean,
        pageable: Pageable
    ): Page<Group>

    fun findAllByNameContainingAndRegion(
        name: String?,
        province: String?,
        city: String?,
        town: String?,
        disabled: Boolean
    ): List<Group>

    fun findAllByNameContainingAndRegion(
        name: String?,
        province: String?,
        city: String?,
        town: String?,
        disabled: Boolean,
        pageable: Pageable
    ): Page<Group>

    fun findAllByCategoryAndNameContainingAndRegion(
        categoryName: String?,
        name: String?,
        province: String?,
        city: String?,
        town: String?,
        disabled: Boolean
    ): List<Group>

    fun findAllByCategoryAndNameContainingAndRegion(
        categoryName: String?,
        name: String?,
        province: String?,
        city: String?,
        town: String?,
        disabled: Boolean,
        pageable: Pageable
    ): Page<Group>

    fun findAllByCategoryAndRecruitStatusAndNameContainingAndRegion(
        categoryName: String?,
        recruitStatus: String?,
        name: String?,
        province: String?,
        city: String?,
        town: String?,
        disabled: Boolean
    ): List<Group>

    fun findAllByCategoryAndRecruitStatusAndNameContainingAndRegion(
        categoryName: String?,
        recruitStatus: String?,
        name: String?,
        province: String?,
        city: String?,
        town: String?,
        disabled: Boolean,
        pageable: Pageable
    ): Page<Group>
}