package com.app.backend.global.error

import com.app.backend.global.dto.response.ApiResponse
import com.app.backend.global.error.exception.DomainException
import com.app.backend.global.error.exception.GlobalErrorCode
import io.github.oshai.kotlinlogging.KLogger
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.http.ResponseEntity
import org.springframework.validation.BindException
import org.springframework.web.HttpRequestMethodNotSupportedException
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.method.annotation.HandlerMethodValidationException

@RestControllerAdvice
class GlobalExceptionHandler {
    private val log: KLogger = KotlinLogging.logger { }

    /**
     * 지원하지 않는 HTTP 메서드 호출 시
     *
     * @param e
     * @return
     */
    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleMethodArgumentNotValidException(e: MethodArgumentNotValidException): ResponseEntity<ApiResponse<Unit>> {
        log.error(e) { "handleMethodArgumentNotValidException" }
        val errorCode = GlobalErrorCode.METHOD_NOT_ALLOWED
        return ResponseEntity.status(errorCode.status)
            .body(ApiResponse.of(false, errorCode.code, errorCode.message))
    }

    /**
     * 지원하지 않는 HTTP 메서드 호출 시
     *
     * @param e
     * @return
     */
    @ExceptionHandler(HttpRequestMethodNotSupportedException::class)
    fun handleHttpRequestMethodNotSupportedException(e: HttpRequestMethodNotSupportedException): ResponseEntity<ApiResponse<Unit>> {
        log.error(e) { "handleHttpRequestMethodNotSupportedException" }
        val errorCode = GlobalErrorCode.METHOD_NOT_ALLOWED
        return ResponseEntity.status(errorCode.status)
            .body(ApiResponse.of(false, errorCode.code, errorCode.message))
    }

    /**
     * HandlerMethodValidationException 발생 시(단일 값, @Valid 또는 @Validated 에서 바인딩 에러)
     *
     * @param e
     * @return
     */
    @ExceptionHandler(HandlerMethodValidationException::class)
    fun handleHandlerMethodValidationException(e: HandlerMethodValidationException): ResponseEntity<ApiResponse<Unit>> {
        log.error(e) { "handleHandlerMethodValidationException" }
        val errorCode = GlobalErrorCode.INVALID_INPUT_VALUE
        return ResponseEntity.status(errorCode.status)
            .body(ApiResponse.of(false, errorCode.code, errorCode.message))
    }

    /**
     * BindException 발생 시(객체, @Valid 또는 @Validated 에서 바인딩 에러)
     *
     * @param e
     * @return
     */
    @ExceptionHandler(BindException::class)
    fun handleBindException(e: BindException): ResponseEntity<ApiResponse<Unit>> {
        log.error(e) { "handleBindException" }
        val errorCode = GlobalErrorCode.INVALID_INPUT_VALUE
        return ResponseEntity.status(errorCode.status)
            .body(ApiResponse.of(false, errorCode.code, errorCode.message))
    }

    @ExceptionHandler(DomainException::class)
    fun handleDomainException(e: DomainException): ResponseEntity<ApiResponse<Unit>> {
        log.error(e) { "handleDomainException" }
        val errorCode = e.domainErrorCode
        return ResponseEntity.status(e.domainErrorCode.status)
            .body(ApiResponse.of(false, e.domainErrorCode.code, e.domainErrorCode.message))
    }

    @ExceptionHandler(Exception::class)
    fun handleException(e: Exception): ResponseEntity<ApiResponse<Unit>> {
        log.error(e) { "handleException" }
        val errorCode = GlobalErrorCode.INTERNAL_SERVER_ERROR
        return ResponseEntity.status(errorCode.status)
            .body(ApiResponse.of(false, errorCode.code, errorCode.message))
    }
}