package com.app.backend.global.util

import com.app.backend.global.annotation.CustomPageJsonSerializer
import com.app.backend.global.dto.response.ApiResponse
import com.app.backend.global.module.CustomPageModule
import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.data.domain.Page
import org.springframework.http.ResponseEntity
import java.util.concurrent.ConcurrentHashMap

class PageUtil {
    companion object {
        private val objectMapperMap = ConcurrentHashMap<String, ObjectMapper>()
        private val log = KotlinLogging.logger {}

        fun processResponseEntity(
            responseEntity: ResponseEntity<*>,
            annotation: CustomPageJsonSerializer
        ): ResponseEntity<*> =
            when (val body = responseEntity.body) {
                is Page<*> -> ResponseEntity.ok(processPageJson(body, annotation))
                is ApiResponse<*> -> ResponseEntity.ok(processApiResponse(body, annotation))
                else -> responseEntity
            }

        fun processApiResponse(apiResponse: ApiResponse<*>, annotation: CustomPageJsonSerializer): ApiResponse<*> =
            when (val data = apiResponse.data) {
                is Page<*> -> ApiResponse.of(
                    apiResponse.isSuccess,
                    apiResponse.code,
                    apiResponse.message,
                    processPageJson(data, annotation)
                )

                else -> apiResponse
            }

        fun processPageJson(page: Page<*>, annotation: CustomPageJsonSerializer): JsonNode? {
            val key = generateKey(annotation)
            val objectMapper = objectMapperMap.computeIfAbsent(key) {
                log.info { "새로운 ObjectMapper 생성: $it" }
                val mapper = ObjectMapper()
                mapper.registerModules(JavaTimeModule(), CustomPageModule(annotation))
                mapper.enable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
                mapper
            }

            return try {
                val json = objectMapper.writeValueAsString(page)
                objectMapper.readTree(json)
            } catch (e: JsonProcessingException) {
                log.error(e) { "Json processing exception occurred" }
                null
            }
        }

        private fun generateKey(annotation: CustomPageJsonSerializer): String {
            return listOf(
                annotation.content,
                annotation.hasContent,
                annotation.totalPages,
                annotation.totalElements,
                annotation.numberOfElements,
                annotation.size,
                annotation.number,
                annotation.hasPrevious,
                annotation.hasNext,
                annotation.isFirst,
                annotation.isLast,
                annotation.sort,
                annotation.empty
            ).joinToString("_")
        }
    }
}
