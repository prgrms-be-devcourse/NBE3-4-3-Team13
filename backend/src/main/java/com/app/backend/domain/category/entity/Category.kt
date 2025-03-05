package com.app.backend.domain.category.entity

import com.app.backend.domain.group.entity.Group
import jakarta.persistence.*
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedDate
import java.time.LocalDateTime

@Entity
@Table(name = "tbl_categories")
class Category(

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,

    @Column(length = 10, nullable = false, unique = true)
    var name: String,

    @OneToMany(mappedBy = "category", cascade = [CascadeType.ALL], orphanRemoval = true)
    val groups: MutableList<Group> = mutableListOf(),

    var disabled: Boolean = false,

    @CreatedDate
    @Column(nullable = true)
    val createdAt: LocalDateTime? = null,

    @LastModifiedDate
    @Column(nullable = true)
    val modifiedAt: LocalDateTime? = null
) {

    constructor() : this(name = "category")

    constructor(name: String) : this(name = name, disabled = false)

    fun modifyName(newName: String) {
        this.name = newName
    }

    fun softDelete() {
        this.disabled = true
    }

    fun restore() {
        this.disabled = false
    }
}
