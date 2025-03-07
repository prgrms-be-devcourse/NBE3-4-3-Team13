package com.app.backend.domain.group.dto.request

import com.fasterxml.jackson.annotation.JsonAlias
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotBlank

class GroupRequest {
    data class Create(
        @field:NotBlank val name: String,
        @field:NotBlank val province: String,
        @field:NotBlank val city: String,
        @field:NotBlank val town: String,
        @field:NotBlank val description: String,
        @field:Min(1) val maxRecruitCount: Int,
        @field:NotBlank val categoryName: String
    )

    data class Update(
        @field:NotBlank val name: String,
        @field:NotBlank val province: String,
        @field:NotBlank val city: String,
        @field:NotBlank val town: String,
        @field:NotBlank val description: String,
        @field:NotBlank val recruitStatus: String,
        @field:Min(1) val maxRecruitCount: Int,
        @field:NotBlank val categoryName: String
    )

    data class ApproveJoining(
        @field:Min(1) val memberId: Long,
        @param:JsonAlias("accept", "isAccept") val isAccept: Boolean
    )

    data class Permission(
        @field:Min(1) val memberId: Long
    )

    data class Search(
        val categoryName: String? = null,
        val recruitStatus: String? = null,
        val name: String? = null,
        val province: String? = null,
        val city: String? = null,
        val town: String? = null
    )
}