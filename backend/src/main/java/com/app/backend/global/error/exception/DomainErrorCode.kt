package com.app.backend.global.error.exception

import org.springframework.http.HttpStatus

interface DomainErrorCode {
    val status: HttpStatus
    val code: String
    val message: String
}