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
    @get:JvmName("getId")
    val id: Long? = null,

    @Column(name = "username", length = 255)
    @get:JvmName("getUsername")
    val username: String? = null,

    @Column(name = "password", length = 255)
    @get:JvmName("getPassword")
    val password: String? = null,

    @Column(name = "nickname", length = 255)
    @get:JvmName("getNickname")
    val nickname: String? = null,

    @Enumerated(EnumType.STRING)
    @Column(name = "oauth_provider", length = 255)
    @get:JvmName("getProvider")
    val provider: Provider? = null,

    @Column(name = "oauth_provider_id", length = 255)
    @get:JvmName("getOauthProviderId")
    val oauthProviderId: String? = null,

    @Column(name = "role", length = 255)
    @get:JvmName("getRole")
    val role: String? = null,

    @Column(nullable = false)
    @get:JvmName("isDisabled")
    override var disabled: Boolean = false
) : BaseEntity() {

    override fun activate() {
        super.deactivate()
    }

    fun softDelete() = Member(
        id = this.id,
        username = this.username,
        password = this.password,
        nickname = this.nickname,
        role = this.role,
        disabled = true,
        provider = this.provider,
        oauthProviderId = this.oauthProviderId
    )

    fun update(
        password: String? = this.password,
        nickname: String? = this.nickname
    ) = Member(
        id = this.id,
        username = this.username,
        password = password,
        nickname = nickname,
        role = this.role,
        disabled = this.disabled,
        provider = this.provider,
        oauthProviderId = this.oauthProviderId
    )

    enum class Provider {
        LOCAL, KAKAO
        // 필요시 NAVER, GOOGLE 등 추가
    }

    companion object {
        @JvmStatic
        fun create(
            username: String?,
            password: String?,
            nickname: String?,
            role: String?,
            disabled: Boolean = false,
            provider: Provider? = null,
            oauthProviderId: String? = null
        ) = Member(
            username = username,
            password = password,
            nickname = nickname,
            role = role,
            disabled = disabled,
            provider = provider,
            oauthProviderId = oauthProviderId
        )
    }
}