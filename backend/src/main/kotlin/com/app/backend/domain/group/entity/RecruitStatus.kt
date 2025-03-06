package com.app.backend.domain.group.entity

enum class RecruitStatus private constructor(forceStatus: Boolean = false) {
    RECRUITING(false), CLOSED(false);

    var forceStatus: Boolean = forceStatus
        protected set

    fun modifyForceStatus(newForceStatus: Boolean) = apply {
        if (this.forceStatus == newForceStatus) return@apply
        this.forceStatus = newForceStatus
    }
}
