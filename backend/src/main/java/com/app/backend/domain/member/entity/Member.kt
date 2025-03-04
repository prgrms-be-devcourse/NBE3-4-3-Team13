package com.app.backend.domain.member.entity

import com.app.backend.global.entity.BaseEntity
import jakarta.persistence.*
import org.hibernate.annotations.DynamicInsert

@Entity
@Table(name = "tbl_members")
@DynamicInsert
data class Member(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "member_id", columnDefinition = "BIGINT")
    val id: Long? = null,

    @Column(name = "username", length = 255)
    val username: String? = null,

    @Column(name = "password", length = 255)
    val password: String? = null,

    @Column(name = "nickname", length = 255)
    val nickname: String? = null,

    @Enumerated(EnumType.STRING)
    @Column(name = "oauth_provider", length = 255)
    val provider: Provider? = null,

    @Column(name = "oauth_provider_id", length = 255)
    val oauthProviderId: String? = null,

    @Column(name = "role", length = 255)
    val role: String? = null,

    @Column(nullable = false)
    val disabled: Boolean = false
) : BaseEntity() {

    override fun activate() {
        super.deactivate()
    }

    enum class Provider {
        LOCAL, KAKAO
        // 필요시 NAVER, GOOGLE 등 추가
    }
}