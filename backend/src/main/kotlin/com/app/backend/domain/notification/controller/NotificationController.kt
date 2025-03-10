package com.app.backend.domain.notification.controller

import com.app.backend.domain.member.service.MemberService
import com.app.backend.domain.notification.SseEmitters
import com.app.backend.domain.notification.dto.NotificationMessage
import com.app.backend.domain.notification.service.NotificationService
import com.app.backend.global.dto.response.ApiResponse
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.*
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter
import java.io.IOException

private val log = LoggerFactory.getLogger(NotificationController::class.java)

@RestController
@RequestMapping("/api/v1/notifications")
class NotificationController(
    private val notificationService: NotificationService,
    private val sseEmitters: SseEmitters,
    private val memberService: MemberService
) {
    @GetMapping(value = ["/subscribe"], produces = [MediaType.TEXT_EVENT_STREAM_VALUE])
    fun subscribe(
        @RequestHeader("Authorization") token: String
    ): SseEmitter {
        val member = memberService.getCurrentMember(token)
        val userId = member.id.toString()
        
        // 기존 연결 강제 종료
        sseEmitters.remove(userId)
        
        val emitter = SseEmitter(30 * 60 * 1000L)  // 30분 타임아웃
        
        try {
            // 연결 즉시 초기 메시지 전송
            emitter.send(
                SseEmitter.event()
                    .name("connect")
                    .data("Connected! Active connections: ${sseEmitters.getActiveEmittersCount()}")
            )
            
            sseEmitters.add(userId, emitter)
            
            emitter.onCompletion {
                sseEmitters.remove(userId)
                log.info("SSE connection completed for user: {}", userId)
            }
            
            emitter.onTimeout {
                sseEmitters.remove(userId)
                log.info("SSE connection timed out for user: {}", userId)
            }
            
            emitter.onError { e ->
                sseEmitters.remove(userId)
                log.error("SSE connection error for user: {}", userId, e)
            }
            
        } catch (e: Exception) {
            sseEmitters.remove(userId)
            log.error("SSE connection failed for user: {}", userId, e)
            throw e
        }
        
        log.info("New SSE connection established for user: {}. Total active: {}", 
                 userId, sseEmitters.getActiveEmittersCount())
        
        return emitter
    }

    @GetMapping
    fun getNotifications(
        @RequestHeader("Authorization") token: String
    ): ApiResponse<List<NotificationMessage>> {
        val member = memberService.getCurrentMember(token)
        val notifications = notificationService.getNotifications(member.id.toString())
        return ApiResponse.of(
            true,
            HttpStatus.OK,
            "알림 목록 조회 성공",
            notifications
        )
    }

    @PatchMapping("/{notificationId}/read")
    fun markAsRead(
        @PathVariable notificationId: Long
    ): ApiResponse<Void> {
        notificationService.markAsRead(notificationId)
        return ApiResponse.of(
            true,
            HttpStatus.OK,
            "알림 읽음 처리 성공"
        )
    }
}