package com.app.backend.domain.group.repository

import com.app.backend.domain.group.entity.Group
import jakarta.persistence.LockModeType
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Lock
import org.springframework.data.jpa.repository.Query
import java.util.*

interface GroupRepository : JpaRepository<Group, Long>, GroupRepositoryCustom {
    fun findByIdAndDisabled(id: Long, disabled: Boolean): Optional<Group>
    fun findAllByDisabled(disabled: Boolean): List<Group>
    fun findAllByDisabled(disabled: Boolean, pageable: Pageable): Page<Group>
    fun findAllByNameContainingAndDisabled(name: String, disabled: Boolean): List<Group>
    fun findAllByNameContainingAndDisabled(name: String, disabled: Boolean, pageable: Pageable): Page<Group>
    fun findAllByCategory_Name(categoryName: String): List<Group>
    fun findAllByCategory_Name(categoryName: String, pageable: Pageable): Page<Group>
    fun findAllByCategory_NameAndDisabled(categoryName: String, disabled: Boolean): List<Group>
    fun findAllByCategory_NameAndDisabled(categoryName: String, disabled: Boolean, pageable: Pageable): Page<Group>

    fun findAllByCategory_NameAndNameContainingAndDisabled(
        categoryName: String,
        name: String,
        disabled: Boolean
    ): List<Group>

    fun findAllByCategory_NameAndNameContainingAndDisabled(
        categoryName: String,
        name: String,
        disabled: Boolean,
        pageable: Pageable
    ): Page<Group>

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT g FROM Group g WHERE g.id = :groupId")
    fun findByIdWithLock(groupId: Long): Optional<Group>
}