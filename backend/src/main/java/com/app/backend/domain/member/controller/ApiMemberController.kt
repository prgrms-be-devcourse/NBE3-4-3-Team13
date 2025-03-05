package com.app.backend.domain.member.controller

import com.app.backend.domain.group.dto.response.GroupMembershipResponse
import com.app.backend.domain.member.dto.request.MemberJoinRequestDto
import com.app.backend.domain.member.dto.request.MemberLoginRequestDto
import com.app.backend.domain.member.dto.request.MemberModifyRequestDto
import com.app.backend.domain.member.dto.response.MemberJoinResponseDto
import com.app.backend.domain.member.dto.response.MemberLoginResponseDto
import com.app.backend.domain.member.dto.response.MemberModifyResponseDto
import com.app.backend.domain.member.entity.Member
import com.app.backend.domain.member.entity.MemberDetails
import com.app.backend.domain.member.service.MemberService
import com.app.backend.domain.member.util.CommonUtil
import com.app.backend.global.dto.response.ApiResponse
import io.swagger.v3.oas.annotations.Operation
import jakarta.servlet.http.Cookie
import jakarta.servlet.http.HttpServletResponse
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*
import java.io.IOException
import java.util.*

@RestController
@RequestMapping("/api/v1/members")
class ApiMemberController(
    private val memberService: MemberService,
    private val util: CommonUtil
) {
    @Operation(summary = "관리자 회원가입", description = "관리자 회원 가입을 진행합니다")
    @PostMapping
    fun join(
        @RequestBody @Valid request: MemberJoinRequestDto
    ): ApiResponse<MemberJoinResponseDto> {
        val response = memberService.createMember(
            request.username,
            request.password,
            request.nickname
        )
        return ApiResponse.of(
            true,
            "MEMBER_CREATE_SUCCESS",
            "회원가입이 성공적으로 완료되었습니다.",
            response
        )
    }

    @Operation(summary = "관리자 로그인", description = "관리자 로그인을 진행합니다")
    @PostMapping("/login")
    @Throws(IOException::class)
    fun login(
        @RequestBody request: MemberLoginRequestDto,
        response: HttpServletResponse
    ): ApiResponse<MemberLoginResponseDto> {
        val loginResult = memberService.login(request)

        // 쿠키 설정
        val refreshTokenCookie = Cookie("refreshToken", loginResult.refreshToken)
        util.setCookies(refreshTokenCookie, response)

        return ApiResponse.of(
            true,
            "MEMBER_LOGIN_SUCCESS",
            "로그인이 성공적으로 완료되었습니다.",
            loginResult
        )
    }

    @Operation(summary = "로그아웃", description = "클라이언트의 토큰 무효화 처리")
    @PostMapping("/logout")
    fun logout(
        @RequestHeader("Authorization") token: String,
        response: HttpServletResponse
    ): ApiResponse<Void> {
        memberService.logout(token)
        util.invalidateCookies(response)
        return ApiResponse.of(
            true,
            "MEMBER_LOGOUT_SUCCESS",
            "로그아웃이 성공적으로 완료되었습니다."
        )
    }

    @Operation(summary = "개인정보 조회", description = "JWT 토큰을 이용해 회원 정보를 조회합니다.")
    @GetMapping("/info")
    fun getMemberInfo(
        @RequestHeader("Authorization") token: String
    ): ApiResponse<MemberDetails> {
        val member = memberService.getCurrentMember(token)
        return ApiResponse.of(
            true,
            "MEMBER_INFO_SUCCESS",
            "회원정보 조회에 성공했습니다",
            MemberDetails.of(member)
        )
    }

    @GetMapping("/mygroups")
    fun getMyGroup(
        @RequestHeader("Authorization") token: String
    ): ApiResponse<List<GroupMembershipResponse.Detail>> {
        val list = memberService.getMyGroup(token)
        return ApiResponse.of(
            true,
            HttpStatus.OK,
            "모임 멤버십 조회에 성공했습니다.",
            list
        )
    }

    @Operation(summary = "개인정보 수정", description = "관리자 회원 정보를 수정합니다")
    @PatchMapping("/modify")
    fun modifyMemberInfo(
        @RequestHeader("Authorization") token: String,
        @RequestBody request: MemberModifyRequestDto
    ): ApiResponse<MemberModifyResponseDto> {
        val member = memberService.getCurrentMember(token)
        val response = memberService.modifyMember(member, request)
        return ApiResponse.of(
            true,
            "MEMBER_MODIFY_SUCCESS",
            "회원정보 수정에 성공했습니다",
            response
        )
    }

    @Operation(summary = "모든 회원 조회", description = "관리자가 모든 회원을 조회합니다")
    @GetMapping("/findAll")
    fun findAll(
        @RequestHeader("Authorization") token: String
    ): ApiResponse<Optional<List<Member>>> {
        val allMembers = memberService.findAllMembers(token)
        return ApiResponse.of(
            true,
            "MEMBER_FIND_ALL_SUCCESS",
            "모든 회원을 조회했습니다",
            allMembers
        )
    }

    @Operation(summary = "회원 탈퇴 (soft delete)", description = "가입자가 회원 탈퇴를 하면 disabled 상태가 됩니다")
    @DeleteMapping
    fun deleteMember(
        @RequestHeader("Authorization") token: String
    ): ApiResponse<Void> {
        memberService.deleteMember(token)
        return ApiResponse.of(
            true,
            "MEMBER_DELETE_SUCCESS",
            "회원 탈퇴에 성공했습니다"
        )
    }
}