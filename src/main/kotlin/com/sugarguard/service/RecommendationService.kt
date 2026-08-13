package com.sugarguard.service

import com.sugarguard.dto.*
import org.springframework.stereotype.Service
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.UUID

@Service
class RecommendationService(
    private val environmentService: EnvironmentService,
    private val placeSearchService: PlaceSearchService,
    // 💡 1. 제미나이 서비스를 주입받습니다.
    private val geminiService: GeminiService
) {
    fun getRecommendation(request: RecommendationRequest): RecommendationResponse {
        val seoulZoneId = ZoneId.of("Asia/Seoul")
        val now = LocalDateTime.now(seoulZoneId).toString()

        // 1. 공공 데이터 API 호출
        val (temperature, isRaining) = environmentService.getWeatherData(request.latitude, request.longitude)
        val pmGrade = environmentService.getFineDustGrade()
        val weatherCondition = if (isRaining) "RAIN" else "CLEAR"

        // 2. 야외 활동 적합 판단 로직
        val isOutdoorSuitable = !isRaining && (pmGrade == "GOOD" || pmGrade == "MODERATE")

        var nearbyParks = emptyList<ParkDto>()
        var activityType = "INDOOR_SQUAT"
        var activityName = "스쿼트 15분"
        var reason = "안전한 실내 활동을 추천해요."
        var location: LocationDto? = null

        // 3. 분기 처리: 공원 존재 여부 확인
        if (isOutdoorSuitable) {
            nearbyParks = placeSearchService.findNearbyParks(request.latitude, request.longitude)

            if (nearbyParks.isNotEmpty()) {
                val park = nearbyParks.first()
                activityType = "OUTDOOR_WALK"
                activityName = "${park.name} 15분 산책"
                reason = "현재 날씨가 맑고 미세먼지가 좋아서 야외 산책에 적합해요."
                location = LocationDto(park.name, park.latitude, park.longitude)
            } else {
                activityType = "INDOOR_STAIRS"
                activityName = "계단 오르내리기"
                reason = "날씨는 좋지만 주변에 공원이 없어 실내 활동을 추천해요."
            }
        } else {
            activityType = "INDOOR_STAIRS"
            activityName = "계단 오르내리기"
            reason = if (isRaining) "현재 비가 오고 있어서 실내 활동을 추천해요." else "현재 미세먼지가 나빠서 실내 활동을 추천해요."
        }

        //  4. 제미나이에게 멘트 생성 요청하기
        val dynamicGuideText = geminiService.generateCoachingText(
            weatherCondition = weatherCondition,
            pmGrade = pmGrade,
            activityName = activityName
        )

        // 5. 최종 응답 조립
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
                // 6. 딱딱했던 고정 텍스트 대신, 제미나이가 만들어준 멘트를 넣어줍니다
                guideText = dynamicGuideText
            )
        )
    }
}