package com.app.backend.domain.group.controller

import com.app.backend.domain.group.service.WebClientService
import com.app.backend.global.dto.response.ApiResponse
import com.fasterxml.jackson.databind.JsonNode
import io.github.oshai.kotlinlogging.KLogger
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Mono

@RestController
@RequestMapping(
    value = ["/api/v1/proxy/kakao"],
    produces = [MediaType.APPLICATION_JSON_VALUE]
)
class KakaoProxyController(private val webClientService: WebClientService) {
    private val log: KLogger = KotlinLogging.logger { }

    @GetMapping("/geo")
    fun getRegionCode(@RequestParam x: Double, @RequestParam y: Double): Mono<ApiResponse<JsonNode>> =
        webClientService.fetchKakaoMapData(x, y).map {
            log.info { "response: $it" }
            ApiResponse.of(true, HttpStatus.OK, "카카오맵 위치 정보 확인 완료", it)
        }

    @GetMapping("/address")
    fun getCoordinatesByAddress(
        @RequestParam province: String,
        @RequestParam city: String,
        @RequestParam town: String
    ): Mono<ApiResponse<JsonNode>> = webClientService
        .fetchKakaoAddressByKeyword(province, city, town)
        .map {
            log.info { "response: $it" }
            ApiResponse.of(true, HttpStatus.OK, "카카오맵 주소 좌표 변환 완료", it)
        }

    @GetMapping("/region")
    fun searchAddress(@RequestParam keyword: String): Mono<ApiResponse<JsonNode>> = webClientService
        .fetchKakaoAddressByKeyword(keyword)
        .map {
            log.info { "response: $it" }
            ApiResponse.of(true, HttpStatus.OK, "카카오맵 주소 검색 완료", it)
        }
}