package com.sugarguard.service

import com.sugarguard.dto.*
import org.springframework.stereotype.Service
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

@Service
class RecommendationService(
    private val placeSearchService: PlaceSearchService,
    private val environmentService: EnvironmentService
) {
    fun createRecommendation(request: RecommendationRequest): RecommendationResponse {

        // 1. 재료 준비: 날씨, 공원, 미세먼지 데이터를 모두 가져옵니다.
        val (temperature, isRaining) = environmentService.getWeatherData(request.latitude, request.longitude)
        val pmGrade = environmentService.getFineDustGrade() // 미세먼지 추가!
        val nearbyParks = placeSearchService.findNearbyParks(request.latitude, request.longitude)

        // 2. 환경 정보 조립
        val environmentDto = EnvironmentDto(
            retrievedAt = ZonedDateTime.now().format(DateTimeFormatter.ISO_OFFSET_DATE_TIME),
            dataSource = "LIVE",
            weatherCondition = if (isRaining) "RAIN" else "CLEAR",
            temperature = temperature,
            feelsLikeTemperature = temperature,
            pmGrade = pmGrade,
            nearbyParks = nearbyParks
        )

        // 3. AI 판단 로직 (단일 활동 추천 결정)
        val recommendationDto: RecommendationDto

        // 미세먼지가 나쁘거나 매우 나쁜지 확인
        val isBadAir = pmGrade == "BAD" || pmGrade == "VERY_BAD"

        // 야외 조건 미충족 (비가 오거나, 미세먼지가 나쁘거나, 공원이 없음)
        if (isRaining || isBadAir || nearbyParks.isEmpty()) {
            recommendationDto = RecommendationDto(
                activityId = "rec_indoor_${System.currentTimeMillis()}",
                activityType = if (isBadAir) "INDOOR_STAIRS" else "INDOOR_SQUAT",
                activityName = if (isBadAir) "계단 오르내리기" else "스쿼트 15분",
                durationMinutes = 15,
                reason = when {
                    isRaining -> "현재 비가 와서 실내 활동을 추천해요."
                    isBadAir -> "현재 미세먼지가 나쁨 수준이라 실내 활동을 추천해요."
                    else -> "주변에 적당한 공원이 없어 실내 활동을 추천해요."
                },
                location = null,
                guideText = "안전한 실내에서 15분간 활동해 보세요."
            )
        }
        // 야외 조건 충족
        else {
            val targetPark = nearbyParks.first()
            recommendationDto = RecommendationDto(
                activityId = "rec_outdoor_${System.currentTimeMillis()}",
                activityType = "OUTDOOR_WALK",
                activityName = "${targetPark.name} 15분 산책",
                durationMinutes = 15,
                reason = "날씨가 맑고 미세먼지도 좋아 야외 산책에 적합해요.",
                location = LocationDto(
                    name = targetPark.name,
                    latitude = targetPark.latitude,
                    longitude = targetPark.longitude
                ),
                guideText = null
            )
        }

        return RecommendationResponse(
            success = true,
            environment = environmentDto,
            recommendation = recommendationDto
        )
    }
}