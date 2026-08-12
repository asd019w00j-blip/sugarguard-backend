package com.sugarguard.service

import com.sugarguard.dto.*
import org.springframework.stereotype.Service
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.UUID

@Service
class RecommendationService(
    private val environmentService: EnvironmentService,
    private val placeSearchService: PlaceSearchService
) {
    fun getRecommendation(request: RecommendationRequest): RecommendationResponse {
        val seoulZoneId = ZoneId.of("Asia/Seoul")
        val now = LocalDateTime.now(seoulZoneId).toString()

        // 1. 공공 데이터 API 호출
        val (temperature, isRaining) = environmentService.getWeatherData(request.latitude, request.longitude)
        val pmGrade = environmentService.getFineDustGrade()
        val weatherCondition = if (isRaining) "RAIN" else "CLEAR"

        // 2. 야외 활동 적합 판단 로직 (비가 안 오고 미세먼지가 좋음/보통일 때)
        val isOutdoorSuitable = !isRaining && (pmGrade == "GOOD" || pmGrade == "MODERATE")

        var nearbyParks = emptyList<ParkDto>()
        var activityType = "INDOOR_SQUAT"
        var activityName = "스쿼트 15분"
        var reason = "안전한 실내 활동을 추천해요."
        var location: LocationDto? = null
        var guideText: String? = "제자리에서 스쿼트를 15분간 진행해 보세요."

        // 3. 분기 처리: 공원 존재 여부 확인
        if (isOutdoorSuitable) {
            nearbyParks = placeSearchService.findNearbyParks(request.latitude, request.longitude)

            if (nearbyParks.isNotEmpty()) {
                val park = nearbyParks.first() // 가장 가까운 공원 선택
                activityType = "OUTDOOR_WALK"
                activityName = "${park.name} 15분 산책"
                reason = "현재 날씨가 맑고 미세먼지가 좋아서 야외 산책에 적합해요."
                location = LocationDto(park.name, park.latitude, park.longitude)
                guideText = null
            } else {
                activityType = "INDOOR_STAIRS"
                activityName = "계단 오르내리기"
                reason = "날씨는 좋지만 주변에 공원이 없어 실내 활동을 추천해요."
                guideText = "가까운 계단에서 15분간 오르내려 보세요."
            }
        } else {
            activityType = "INDOOR_STAIRS"
            activityName = "계단 오르내리기"
            reason = if (isRaining) "현재 비가 오고 있어서 실내 활동을 추천해요." else "현재 미세먼지가 나빠서 실내 활동을 추천해요."
            guideText = "가까운 계단에서 15분간 오르내려 보세요."
        }

        // 4. API 명세서 규격에 맞춘 최종 응답 조립[cite: 2]
        return RecommendationResponse(
            success = true,
            environment = EnvironmentDto(
                retrievedAt = now,
                dataSource = "LIVE",
                weatherCondition = weatherCondition,
                temperature = temperature,
                feelsLikeTemperature = temperature + 0.5,
                pmGrade = pmGrade,
                nearbyParks = nearbyParks
            ),
            recommendation = RecommendationDataDto(
                activityId = "rec_${UUID.randomUUID().toString().substring(0, 8)}",
                activityType = activityType,
                activityName = activityName,
                durationMinutes = 15,
                reason = reason,
                location = location,
                guideText = guideText
            )
        )
    }
}