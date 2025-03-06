package com.app.backend.domain.chat.room.controller

import com.app.backend.domain.group.entity.Group
import com.app.backend.domain.member.entity.Member
import com.app.backend.global.entity.BaseEntity
import jakarta.persistence.*

@Entity
@Table(name = "tbl_meeting_applications")
class MeetingApplication(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "meeting_application_id", nullable = false)
    val id: Long? = null,

    @Column(columnDefinition = "TEXT", nullable = false)
    var context: String,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "group_id", nullable = false)
    val group: Group,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    val member: Member
) : BaseEntity() {

    fun modifyContext(newContext: String): MeetingApplication {
        if (context != newContext) {
            context = newContext
        }
        return this
    }
}
