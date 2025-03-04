package com.app.backend.domain.comment.exception;

import org.springframework.http.HttpStatus;

import com.app.backend.global.error.exception.DomainErrorCode;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum CommentErrorCode implements DomainErrorCode {

	COMMENT_NOT_FOUND(HttpStatus.NOT_FOUND, "CM001", "댓글을 찾을 수 없습니다"),
	COMMENT_CREATION_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "CM002", "댓글 생성에 실패했습니다"),
	COMMENT_ACCESS_DENIED(HttpStatus.FORBIDDEN, "CM003", "댓글에 대한 권한이 없습니다"),
	COMMENT_INVALID_CONTENT(HttpStatus.BAD_REQUEST, "CM004", "댓글 내용이 유효하지 않습니다");




	private final HttpStatus status;
	private final String code;
	private final String message;
}
