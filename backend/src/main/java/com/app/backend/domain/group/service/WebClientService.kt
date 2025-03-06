package com.app.backend.domain.group.service

import com.fasterxml.jackson.databind.JsonNode
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpHeaders
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClient
import reactor.core.publisher.Mono

@Service
class WebClientService(private val webClientBuilder: WebClient.Builder) {
    private val KAKAO_MAP_API_URL = "https://dapi.kakao.com/v2/local"

    @Value("\${kakao.rest-api.key}")
    private lateinit var kakaoJsKey: String

    fun fetchKakaoMapData(x: Double, y: Double): Mono<JsonNode> = getKakaoMapWebClient()
        .get()
        .uri {
            it.path("/geo/coord2address.json")
                .queryParam("x", x)
                .queryParam("y", y)
                .queryParam("input_coord", "WGS84")
                .build()
        }
        .retrieve()
        .bodyToMono(JsonNode::class.java)

    fun fetchKakaoAddressByKeyword(province: String, city: String, town: String): Mono<JsonNode> =
        getKakaoMapWebClient()
            .get()
            .uri {
                it.path("/search/address.json")
                    .queryParam("query", "$province $city $town")
                    .build()
            }
            .retrieve()
            .bodyToMono(JsonNode::class.java)

    fun fetchKakaoAddressByKeyword(keyword: String) = getKakaoMapWebClient()
        .get()
        .uri {
            it.path("/search/keyword.json")
                .queryParam("query", keyword)
                .build()
        }
        .retrieve()
        .bodyToMono(JsonNode::class.java)

    //==================== 내부 함수 ====================//
    private fun getKakaoMapWebClient(): WebClient = webClientBuilder.baseUrl(KAKAO_MAP_API_URL)
        .defaultHeader(HttpHeaders.AUTHORIZATION, "KakaoAK $kakaoJsKey").build()
}