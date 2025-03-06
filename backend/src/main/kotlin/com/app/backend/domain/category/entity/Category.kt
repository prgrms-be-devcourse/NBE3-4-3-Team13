package com.app.backend.domain.category.entity

import com.app.backend.domain.group.entity.Group
import com.app.backend.global.entity.BaseEntity
import jakarta.persistence.*

@Entity
@Table(name = "tbl_categories")
class Category (
    @Column(length = 10, nullable = false, unique = true)
    var name: String
): BaseEntity() {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "category_id")
    var id: Long = 0L

    @OneToMany(mappedBy = "category")
    val groups: MutableList<Group> = mutableListOf()

    fun modifyName(name: String) {
        this.name = name
    }

    fun softDelete() {
        this.deactivate()
    }
}