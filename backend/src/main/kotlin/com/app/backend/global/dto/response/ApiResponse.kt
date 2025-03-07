package com.app.backend.global.dto.response

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty
import org.springframework.http.HttpStatus

@JsonInclude(JsonInclude.Include.NON_NULL)
class ApiResponse<T> private constructor(
    @get:JsonProperty("isSuccess") val isSuccess: Boolean,
    val code: String,
    val message: String,
    val data: T?
) {
    companion object {
        fun <T> of(isSuccess: Boolean, code: String, message: String) =
            ApiResponse<T>(isSuccess, code, message, null)

        fun <T> of(isSuccess: Boolean, status: HttpStatus, message: String) =
            ApiResponse<T>(isSuccess, status.value().toString(), message, null)

        fun <T> of(isSuccess: Boolean, code: String, message: String, data: T?) =
            ApiResponse<T>(isSuccess, code, message, data)

        fun <T> of(isSuccess: Boolean, status: HttpStatus, message: String, data: T?) =
            ApiResponse<T>(isSuccess, status.value().toString(), message, data)
    }
}