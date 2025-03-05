package com.app.backend.domain.category.entity

import com.app.backend.domain.group.entity.Group
import jakarta.persistence.*

@Entity
@Table(name = "tbl_categories")
class Category (
    @Column(length = 10, nullable = false, unique = true)
    var name: String
) {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "category_id")
    var id: Long = 0L

    @OneToMany(mappedBy = "category")
    val groups: MutableList<Group> = mutableListOf()

    @Column(nullable = false)
    var disabled: Boolean = false

    fun modifyName(name: String) {
        this.name = name
    }

    fun softDelete() {
        this.disabled = true
    }
}