package com.app.backend.domain.group.entity

import com.app.backend.domain.category.entity.Category
import com.app.backend.domain.chat.room.entity.ChatRoom
import com.app.backend.domain.meetingApplication.entity.MeetingApplication
import com.app.backend.global.entity.BaseEntity
import jakarta.persistence.*
import jakarta.validation.constraints.Min

@Entity
@Table(name = "tbl_groups")
class Group private constructor(
    name: String,
    province: String,
    city: String,
    town: String,
    description: String,
    status: RecruitStatus = RecruitStatus.RECRUITING,
    maxRecruitCount: Int,
    category: Category,
    likeCount: Int = 0
) : BaseEntity() {
    companion object {
        fun of(
            name: String,
            province: String,
            city: String,
            town: String,
            description: String,
            status: RecruitStatus? = RecruitStatus.RECRUITING,
            maxRecruitCount: Int,
            category: Category
        ) = Group(
            name,
            province,
            city,
            town,
            description,
            status ?: RecruitStatus.RECRUITING,
            maxRecruitCount,
            category
        )
    }

    init {
        setRelationshipWithCategory(category)
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "group_id")
    val id: Long? = null

    @Column(nullable = false)
    var name: String = name
        protected set

    @Column(nullable = false)
    var province: String = province
        protected set

    @Column(nullable = false)
    var city: String = city
        protected set

    @Column(nullable = false)
    var town: String = town
        protected set

    @Column(columnDefinition = "TEXT", nullable = false)
    var description: String = description
        protected set

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var recruitStatus: RecruitStatus = status
        protected set

    @Column(nullable = false)
    @field:Min(1)
    var maxRecruitCount: Int = maxRecruitCount
        protected set

    @OneToMany(mappedBy = "group")
    val members: MutableList<GroupMembership> = mutableListOf()

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "chat_room_id")
    var chatRoom: ChatRoom? = null

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    var category: Category = category
        protected set

    @OneToMany(mappedBy = "group", cascade = [CascadeType.REMOVE])
    var likes: MutableList<GroupLike> = mutableListOf()
        protected set

    @Column(nullable = false)
    var likeCount: Int = likeCount
        protected set

    @OneToMany(mappedBy = "group", cascade = [CascadeType.REMOVE])
    var meetingApplications: MutableList<MeetingApplication> = mutableListOf()
        protected set

    //==================== 연관관계 함수 ====================//

    private fun setRelationshipWithCategory(category: Category) {
        this.category = category
        category.groups.add(this)
    }

    //==================== 모임(Group) 수정 함수 ====================//

    /**
     * 모임명 수정
     *
     * @param newName - 새로운 모임 이름
     * @return this
     */
    fun modifyName(newName: String) = apply {
        if (newName.isBlank() || this.name == newName) return@apply
        this.name = newName
    }

    /**
     * 모임 활동 지역 수정
     *
     * @param newProvince - 새로운 모임 활동 지역: 시/도
     * @param newCity     - 새로운 모임 활동 지역: 시/군/구
     * @param newTown     - 새로운 모임 활동 지역: 읍/면/동
     * @return this
     */
    fun modifyRegion(newProvince: String, newCity: String, newTown: String) = apply {
        if (newProvince.isBlank() || newCity.isBlank() || newTown.isBlank() || (this.province == newProvince && this.city == newCity && this.town == newTown)) return@apply
        this.province = newProvince
        this.city = newCity
        this.town = newTown
    }

    /**
     * 모임 정보 수정
     *
     * @param newDescription - 새로운 모임 정보
     * @return this
     */
    fun modifyDescription(newDescription: String) = apply {
        if (newDescription.isBlank() || this.description == newDescription) return@apply
        this.description = newDescription
    }

    /**
     * 모집 상태 수정
     *
     * @param newRecruitStatus - 새로운 모집 상태
     * @return this
     */
    fun modifyRecruitStatus(newRecruitStatus: RecruitStatus) = apply {
        if (this.recruitStatus == newRecruitStatus) return@apply
        this.recruitStatus = newRecruitStatus
    }

    /**
     * 최대 모집 인원 수정
     *
     * @param newMaxRecruitCount - 새로운 최대 모집 인원
     * @return this
     */
    fun modifyMaxRecruitCount(newMaxRecruitCount: Int) = apply {
        if (this.maxRecruitCount == newMaxRecruitCount) return@apply
        this.maxRecruitCount = newMaxRecruitCount
    }

    /**
     * 카테고리 수정
     *
     * @param newCategory - 새로운 카테고리
     * @return this
     */
    fun modifyCategory(newCategory: Category) = apply {
        if (this.category.name == newCategory.name) return@apply
        this.category = newCategory
    }

    /**
     * 좋아요 수 증가
     */
    fun increaseLikeCount() = apply {
        this.likeCount++
    }

    /**
     * 좋아요 수 감소
     */
    fun decreaseLikeCount() = apply {
        if (this.likeCount > 0) this.likeCount--
    }

    /**
     * 모임 삭제(Soft Delete)
     */
    fun delete() = run { this.deactivate() }
}
