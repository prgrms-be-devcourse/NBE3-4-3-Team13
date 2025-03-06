package com.app.backend.domain.group.entity

import com.app.backend.domain.member.entity.Member
import com.app.backend.global.entity.BaseEntity
import jakarta.persistence.*

@Entity
@Table(name = "tbl_groupLikes", uniqueConstraints = [UniqueConstraint(columnNames = ["group_id", "member_id"])])
class GroupLike constructor(
    member: Member,
    group: Group
) : BaseEntity() {
    init {
        setRelationshipWithGroup(group)
    }

    @Id
    @Column(name = "group_like_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    var member: Member = member
        protected set

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "group_id", nullable = false)
    var group: Group = group
        protected set

    //==================== 연관관계 함수 ====================//

    private fun setRelationshipWithGroup(group: Group) {
        this.group = group
        group.likes.add(this)
    }
}