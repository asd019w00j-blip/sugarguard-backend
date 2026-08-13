package com.sugarguard.service

import com.sugarguard.dto.*
import com.sugarguard.repository.ActivityRecordRepository
import org.springframework.stereotype.Service
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.UUID

@Service
class RecommendationService(
    private val environmentService: EnvironmentService,
    private val placeSearchService: PlaceSearchService,
    private val geminiService: GeminiService,
    private val aiPredictionService: AiPredictionService,
    // 💡 1. 실제 DB 기록을 꺼내오기 위해 Repository를 주입받습니다.
    private val recordRepository: ActivityRecordRepository
) {
    fun getRecommendation(request: RecommendationRequest): RecommendationResponse {
        val seoulZoneId = ZoneId.of("Asia/Seoul")
        val now = LocalDateTime.now(seoulZoneId).toString()

        // 1. 공공 데이터 API 호출
        val (temperature, isRaining) = environmentService.getWeatherData(request.latitude, request.longitude)
        val pmGradeString = environmentService.getFineDustGrade()
        val weatherCondition = if (isRaining) "RAIN" else "CLEAR"

        // 2. 1차 기본 추천 판단 로직
        val isOutdoorSuitable = !isRaining && (pmGradeString == "GOOD" || pmGradeString == "MODERATE")
        var nearbyParks = emptyList<ParkDto>()

        var initialActivityType = "INDOOR_SQUAT"
        var initialActivityName = "스쿼트 15분"
        var location: LocationDto? = null

        if (isOutdoorSuitable) {
            nearbyParks = placeSearchService.findNearbyParks(request.latitude, request.longitude)
            if (nearbyParks.isNotEmpty()) {
                val park = nearbyParks.first()
                initialActivityType = "OUTDOOR_WALK"
                initialActivityName = "${park.name} 15분 산책"
                location = LocationDto(park.name, park.latitude, park.longitude)
            } else {
                initialActivityType = "INDOOR_STAIRS"
                initialActivityName = "계단 오르내리기"
            }
        } else {
            initialActivityType = "INDOOR_STAIRS"
            initialActivityName = "계단 오르내리기"
        }

        // 3. 실제 DB 기반 개인 유저 통계 계산 시작
        val aiRaining = if (isRaining) 1 else 0
        val aiPmGrade = when(pmGradeString) {
            "GOOD" -> 1
            "MODERATE" -> 2
            "BAD" -> 3
            "VERY_BAD" -> 4
            else -> 1
        }
        val aiActivityType = when(initialActivityType) {
            "OUTDOOR_WALK" -> 0
            "INDOOR_STAIRS" -> 1
            else -> 2 // INDOOR_SQUAT
        }

        // DB에서 지금까지 유저가 완료한 모든 기록을 가져옵니다.
        val allRecords = recordRepository.findAll()
        val totalCount = allRecords.size.toDouble()

        var userWalkRate = 0.33
        var userStairsRate = 0.33
        var userSquatRate = 0.33

        // 기록이 하나라도 있다면, 각 활동별 선호도 비율을 실시간으로 계산합니다.
        if (totalCount > 0) {
            userWalkRate = allRecords.count { it.activityType == "OUTDOOR_WALK" } / totalCount
            userStairsRate = allRecords.count { it.activityType == "INDOOR_STAIRS" } / totalCount
            userSquatRate = allRecords.count { it.activityType == "INDOOR_SQUAT" } / totalCount
        }

        val aiRequest = AiPredictionRequest(
            temperature = temperature,
            pm_grade = aiPmGrade,
            is_raining = aiRaining,
            activity_type = aiActivityType,
            // 계산된 실제 유저 데이터를 파이썬 서버로 넘겨줍니다.
            user_walk_rate = userWalkRate,
            user_stairs_rate = userStairsRate,
            user_squat_rate = userSquatRate
        )

        // 파이썬 8000 포트로 데이터를 보내어 AI의 예측(0 또는 1)을 받아옵니다.
        val prediction = aiPredictionService.getCompletionPrediction(aiRequest)

        var finalActivityType = initialActivityType
        var finalActivityName = initialActivityName
        var finalReason = ""

        if (prediction == 1) {
            finalReason = "날씨와 회원님의 과거 활동 패턴을 AI가 분석해 가장 달성률이 높은 활동을 추천해요!"
        } else {
            finalActivityType = "INDOOR_SQUAT"
            finalActivityName = "스쿼트 15분"
            location = null
            finalReason = "원래는 다른 활동을 추천하려 했지만, 회원님의 평소 패턴을 AI가 분석해 성공률이 가장 높은 스쿼트로 맞춤 추천해요!"
        }
        // --- AI 연동 로직 끝 ---

        // 4. 제미나이 LLM 코칭 멘트 생성
        val dynamicGuideText = geminiService.generateCoachingText(
            weatherCondition = weatherCondition,
            pmGrade = pmGradeString,
            activityName = finalActivityName
        )

        return RecommendationResponse(
            success = true,
            environment = EnvironmentDto(
                retrievedAt = now,
                dataSource = "LIVE",
                weatherCondition = weatherCondition,
                temperature = temperature,
                feelsLikeTemperature = temperature + 0.5,
                pmGrade = pmGradeString,
                nearbyParks = nearbyParks
            ),
            recommendation = RecommendationDataDto(
                activityId = "rec_${UUID.randomUUID().toString().substring(0, 8)}",
                activityType = finalActivityType,
                activityName = finalActivityName,
                durationMinutes = 15,
                reason = finalReason,
                location = location,
                guideText = dynamicGuideText
            )
        )
    }
}