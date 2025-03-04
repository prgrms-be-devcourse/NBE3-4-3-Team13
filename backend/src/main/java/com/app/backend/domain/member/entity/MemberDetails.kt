package com.app.backend.domain.member.entity

import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.UserDetails
import java.time.format.DateTimeFormatter

class MemberDetails(member: Member) : UserDetails {
    @get:JvmName("getId")
    val id: Long? = member.id
    
    private val memberUsername: String? = member.username
    
    private val memberPassword: String? = member.password
    
    @get:JvmName("getNickname")
    val nickname: String? = member.nickname
    
    @get:JvmName("getProvider")
    val provider: String = member.provider.toString()
    
    @get:JvmName("getRole")
    val role: String? = member.role
    
    @get:JvmName("getCreatedAt")
    val createdAt: String = member.createdAt?.format(DATE_FORMATTER) ?: ""
    
    @get:JvmName("getModifiedAt")
    val modifiedAt: String = member.modifiedAt?.format(DATE_FORMATTER) ?: ""
    
    private val memberDisabled: Boolean = member.disabled

    override fun getAuthorities(): Collection<GrantedAuthority> =
        role?.let { listOf(SimpleGrantedAuthority(it)) } ?: emptyList()

    override fun getPassword(): String = memberPassword ?: ""

    override fun getUsername(): String = memberUsername ?: ""

    override fun isAccountNonExpired(): Boolean = true

    override fun isAccountNonLocked(): Boolean = true

    override fun isCredentialsNonExpired(): Boolean = true

    override fun isEnabled(): Boolean = !memberDisabled

    companion object {
        private val DATE_FORMATTER: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")

        @JvmStatic
        fun of(member: Member): MemberDetails = MemberDetails(member)
    }
} 