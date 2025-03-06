package com.app.backend.domain.group.constant

class GroupMessageConstant {
    companion object {
        const val CREATE_GROUP_SUCCESS = "모임이 성공적으로 생성되었습니다."
        const val READ_GROUP_SUCCESS = "모임이 성공적으로 조회되었습니다."
        const val READ_GROUPS_SUCCESS = "모임 목록이 성공적으로 조회되었습니다."
        const val UPDATE_GROUP_SUCCESS = "모임이 성공적으로 수정되었습니다."
        const val DELETE_GROUP_SUCCESS = "모임이 성공적으로 삭제되었습니다."
        const val LEAVE_GROUP_SUCCESS = "모임에서 성공적으로 탈퇴했습니다."
        const val APPROVE_JOINING_SUCCESS = "모임 신청을 승인했습니다."
        const val REJECT_JOINING_SUCCESS = "모임 신청을 거절했습니다."
        const val MODIFY_GROUP_ROLE_SUCCESS = "모임 내 회원 권한이 성공적으로 변경되었습니다."
    }
}
