package com.app.backend.domain.group.dto.response

import com.app.backend.domain.group.entity.Group
import com.app.backend.domain.group.entity.GroupRole
import com.app.backend.domain.group.entity.MembershipStatus
import com.app.backend.global.util.AppUtil

class GroupResponse {
    companion object {
        fun toDetail(group: Group) = Detail(
            id = group.id!!,
            categoryName = group.category.name,
            name = group.name,
            province = group.province,
            city = group.city,
            town = group.town,
            description = group.description,
            recruitStatus = group.recruitStatus.name,
            maxRecruitStatus = group.maxRecruitCount,
            currentMemberCount = group.members.count { it.status == MembershipStatus.APPROVED && !it.disabled },
            createdAt = AppUtil.localDateTimeToString(group.createdAt),
            groupLeaders = group.members.filter { it.status == MembershipStatus.APPROVED && it.groupRole == GroupRole.LEADER && !it.disabled }
                .map { it.member.nickname!! }
        )

        fun toDetail(group: Group, isApplying: Boolean, isMember: Boolean, isAdmin: Boolean) = Detail(
            id = group.id!!,
            categoryName = group.category.name,
            name = group.name,
            province = group.province,
            city = group.city,
            town = group.town,
            description = group.description,
            recruitStatus = group.recruitStatus.name,
            maxRecruitStatus = group.maxRecruitCount,
            currentMemberCount = group.members.count { it.status == MembershipStatus.APPROVED && !it.disabled },
            createdAt = AppUtil.localDateTimeToString(group.createdAt),
            isApplying = isApplying,
            isMember = isMember,
            isAdmin = isAdmin,
            groupLeaders = group.members.filter { it.status == MembershipStatus.APPROVED && it.groupRole == GroupRole.LEADER && !it.disabled }
                .map { it.member.nickname!! }
        )

        fun toListInfo(group: Group) = ListInfo(
            id = group.id!!,
            categoryName = group.category.name,
            name = group.name,
            province = group.province,
            city = group.city,
            town = group.town,
            recruitStatus = group.recruitStatus.name,
            maxRecruitStatus = group.maxRecruitCount,
            currentMemberCount = group.members.count { it.status == MembershipStatus.APPROVED && !it.disabled },
            createdAt = AppUtil.localDateTimeToString(group.createdAt),
            groupLeaders = group.members.filter { it.status == MembershipStatus.APPROVED && it.groupRole == GroupRole.LEADER && !it.disabled }
                .map { it.member.nickname!! }
        )
    }

    data class Detail(
        val id: Long,
        val categoryName: String,
        val name: String,
        val province: String,
        val city: String,
        val town: String,
        val description: String,
        val recruitStatus: String,
        val maxRecruitStatus: Int,
        val currentMemberCount: Int,
        val createdAt: String,
        val isApplying: Boolean? = null,
        val isMember: Boolean? = null,
        val isAdmin: Boolean? = null,
        val groupLeaders: List<String>
    )

    data class ListInfo(
        val id: Long,
        val categoryName: String,
        val name: String,
        val province: String,
        val city: String,
        val town: String,
        val recruitStatus: String,
        val maxRecruitStatus: Int,
        val currentMemberCount: Int,
        val createdAt: String,
        val groupLeaders: List<String>
    )
}