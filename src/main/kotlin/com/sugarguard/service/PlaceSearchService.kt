package com.sugarguard.service

import com.sugarguard.dto.KakaoLocalResponse
import com.sugarguard.dto.ParkDto
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClient

@Service
class PlaceSearchService(
    private val webClient: WebClient
) {
    // application.yml에 적어둔 카카오 REST API 키를 불러옵니다.
    @Value("\${kakao.rest-api-key}")
    private lateinit var kakaoRestApiKey: String

    fun findNearbyParks(latitude: Double, longitude: Double): List<ParkDto> {
        return try {
            val response = webClient.get()
                .uri { uriBuilder ->
                    uriBuilder
                        .scheme("https")
                        .host("dapi.kakao.com")
                        .path("/v2/local/search/keyword.json")
                        .queryParam("query", "근린공원")
                        .queryParam("y", latitude)
                        .queryParam("x", longitude)
                        .queryParam("radius", 1000)
                        .queryParam("sort", "distance")
                        .build()
                }
                .header("Authorization", "KakaoAK $kakaoRestApiKey")
                .retrieve()
                .bodyToMono(KakaoLocalResponse::class.java)
                .block()

            val foundParks = response?.documents?.map { doc ->
                ParkDto(
                    name = doc.place_name,
                    latitude = doc.y.toDouble(),
                    longitude = doc.x.toDouble(),
                    distanceMeters = doc.distance.toInt()
                )
            } ?: emptyList()

            // 카카오에 공원이 없으면 자체 데이터 반환
            if (foundParks.isEmpty()) {
                getFallbackCampusParks()
            } else {
                foundParks
            }

        } catch (e: Exception) {
            println("카카오 API 호출 에러: ${e.message}")
            getFallbackCampusParks()
        }
    }

    // 자체 하드코딩 장소 데이터 (API 호출 실패 시 방어용)
    private fun getFallbackCampusParks(): List<ParkDto> {
        return listOf(
            ParkDto(
                name = "한서대학교 조각공원",
                latitude = 36.6891,
                longitude = 126.5772,
                distanceMeters = 300
            ),
            ParkDto(
                name = "영암관 앞 운동장",
                latitude = 36.6885,
                longitude = 126.5760,
                distanceMeters = 150
            )
        )
    }
}