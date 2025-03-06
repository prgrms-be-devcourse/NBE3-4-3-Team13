package com.app.backend.domain.meetingApplication.exception

import com.app.backend.global.error.exception.DomainErrorCode
import lombok.Getter
import lombok.RequiredArgsConstructor
import org.springframework.http.HttpStatus


enum class MeetingApplicationErrorCode (
    private val status: HttpStatus,
    private val code: String,
    private val message: String
) : DomainErrorCode {
    GROUP_NOT_FOUND(HttpStatus.NOT_FOUND, "MA001", "해당 id의 그룹은 존재하지 않습니다."),
    MEMBER_NOT_FOUND(HttpStatus.NOT_FOUND, "MA002", "해당 id의 멤버는 존재하지 않습니다."),
    GROUP_MEMBER_LIMIT_EXCEEDED(HttpStatus.CONFLICT, "MA003", "그룹 정원이 초과되었습니다."),
    UNAUTHORIZED_ACCESS(HttpStatus.UNAUTHORIZED, "MA004", "조회 권한이 없습니다."),
    MEMBER_NOT_FOUND_IN_GROUP(HttpStatus.NOT_FOUND, "MA005", "그룹에 해당 멤버가 없습니다."),
    MEETING_APPLICATION_NOT_FOUND(HttpStatus.NOT_FOUND, "MA005", "해당 id의 meeting application은 존재하지 않습니다."),
    ALREADY_IN_GROUP(HttpStatus.CONFLICT, "MA006", "이미 가입된 회원입니다.");

    override fun getStatus(): HttpStatus = status
    override fun getCode(): String = code
    override fun getMessage(): String = message
}
