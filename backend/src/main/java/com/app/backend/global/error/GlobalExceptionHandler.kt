package com.app.backend.global.error;

import com.app.backend.global.dto.response.ApiResponse;
import com.app.backend.global.error.exception.DomainErrorCode;
import com.app.backend.global.error.exception.DomainException;
import com.app.backend.global.error.exception.GlobalErrorCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.server.MethodNotAllowedException;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 지원하지 않는 HTTP 메서드 호출 시
     *
     * @param e
     * @return
     */
    @ExceptionHandler(MethodNotAllowedException.class)
    public ResponseEntity<ApiResponse<Void>> handleMethodNotAllowedException(MethodNotAllowedException e) {
        log.error("handleMethodNotAllowedException", e);
        final DomainErrorCode errorCode = GlobalErrorCode.METHOD_NOT_ALLOWED;
        return ResponseEntity.status(errorCode.getStatus())
                             .body(ApiResponse.of(false, errorCode.getCode(), errorCode.getMessage()));
    }

    /**
     * 지원하지 않는 HTTP 메서드 호출 시
     *
     * @param e
     * @return
     */
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    private ResponseEntity<ApiResponse<Void>> handleHttpRequestMethodNotSupportedException(
            HttpRequestMethodNotSupportedException e
    ) {
        log.error("handleHttpRequestMethodNotSupportedException", e);
        final DomainErrorCode errorCode = GlobalErrorCode.METHOD_NOT_ALLOWED;
        return ResponseEntity.status(errorCode.getStatus())
                             .body(ApiResponse.of(false, errorCode.getCode(), errorCode.getMessage()));
    }

    /**
     * HandlerMethodValidationException 발생 시(단일 값, @Valid 또는 @Validated 에서 바인딩 에러)
     *
     * @param e
     * @return
     */
    @ExceptionHandler(HandlerMethodValidationException.class)
    public ResponseEntity<ApiResponse<Void>> handleHandlerMethodValidationException(
            HandlerMethodValidationException e) {
        log.error("handleHandlerMethodValidationException", e);
        final DomainErrorCode errorCode = GlobalErrorCode.INVALID_INPUT_VALUE;
        return ResponseEntity.status(errorCode.getStatus())
                             .body(ApiResponse.of(false, errorCode.getCode(), errorCode.getMessage()));
    }

    /**
     * BindException 발생 시(객체, @Valid 또는 @Validated 에서 바인딩 에러)
     *
     * @param e
     * @return
     */
    @ExceptionHandler(BindException.class)
    public ResponseEntity<ApiResponse<Void>> handleBindException(BindException e) {
        log.error("handleBindException", e);
        final DomainErrorCode errorCode = GlobalErrorCode.INVALID_INPUT_VALUE;
        return ResponseEntity.status(errorCode.getStatus())
                             .body(ApiResponse.of(false, errorCode.getCode(), errorCode.getMessage()));
    }

    @ExceptionHandler(DomainException.class)
    public ResponseEntity<ApiResponse<Void>> handleDomainException(DomainException e) {
        log.error("handleDomainException", e);
        return ResponseEntity.status(e.getDomainErrorCode().getStatus())
                             .body(ApiResponse.of(false,
                                                  e.getDomainErrorCode().getCode(),
                                                  e.getDomainErrorCode().getMessage()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleException(Exception e) {
        log.error("handleException", e);
        final DomainErrorCode errorCode = GlobalErrorCode.INTERNAL_SERVER_ERROR;
        return ResponseEntity.status(errorCode.getStatus())
                             .body(ApiResponse.of(false, errorCode.getCode(), errorCode.getMessage()));
    }

}
