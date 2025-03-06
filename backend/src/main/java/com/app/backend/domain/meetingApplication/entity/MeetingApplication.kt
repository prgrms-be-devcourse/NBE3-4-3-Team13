package com.app.backend.domain.meetingApplication.entity

import com.app.backend.domain.group.entity.Group
import com.app.backend.domain.member.entity.Member
import com.app.backend.global.entity.BaseEntity
import jakarta.persistence.*

@Entity
@Table(name = "tbl_meeting_applications")
class MeetingApplication (
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "group_id", nullable = false)
    val group: Group,

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "member_id", nullable = false)
    val member: Member,

    @Column(columnDefinition = "TEXT", nullable = false)
    var context: String
) : BaseEntity() {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "meeting_application_id", nullable = false)
    val id: Long = 0L

    fun modifyContext(newContext: String): MeetingApplication {
        if (context != newContext) context = newContext
        return this
    }
}
