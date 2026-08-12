package com.sugarguard.service

import com.sugarguard.dto.ParkDto
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClient

// 카카오 응답을 받기 위한 내부 클래스
private data class KakaoSearchResponse(val documents: List<KakaoDocument>)
private data class KakaoDocument(val place_name: String, val y: String, val x: String, val distance: String)

@Service
class PlaceSearchService(
    private val webClient: WebClient
) {
    @Value("\${kakao.rest-api-key}")
    private lateinit var kakaoRestApiKey: String

    fun findNearbyParks(lat: Double, lon: Double): List<ParkDto> {
        return try {
            val response = webClient.get()
                .uri { uriBuilder ->
                    uriBuilder.scheme("https").host("dapi.kakao.com").path("/v2/local/search/keyword.json")
                        .queryParam("query", "공원")
                        .queryParam("y", lat)
                        .queryParam("x", lon)
                        .queryParam("radius", 2000) // 2km 이내
                        .queryParam("sort", "distance")
                        .build()
                }
                .header("Authorization", "KakaoAK $kakaoRestApiKey")
                .retrieve()
                .bodyToMono(KakaoSearchResponse::class.java)
                .block()

            val documents = response?.documents ?: emptyList()

            // 상위 3개의 공원만 필터링하여 DTO로 변환
            documents.take(3).map {
                ParkDto(
                    name = it.place_name,
                    latitude = it.y.toDouble(),
                    longitude = it.x.toDouble(),
                    distanceMeters = it.distance.toIntOrNull() ?: 0
                )
            }
        } catch (e: Exception) {
            println("⚠️ 카카오 API 공원 검색 실패: ${e.message}")
            emptyList()
        }
    }
}