package com.app.backend.domain.meetingApplication.entity

import com.app.backend.domain.group.entity.Group
import com.app.backend.domain.member.entity.Member
import com.app.backend.global.entity.BaseEntity
import jakarta.persistence.*

@Entity
@Table(name = "tbl_meeting_applications")
class MeetingApplication : BaseEntity() {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "meeting_application_id", nullable = false)
    private var id: Long? = null

    @Column(columnDefinition = "TEXT", nullable = false)
    private var context: String? = null

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "group_id", nullable = false)
    private val group: Group? = null

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private val member: Member? = null

    fun modifyContext(newContext: String): MeetingApplication {
        if (context != newContext) context = newContext
        return this
    }
}
