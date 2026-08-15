package com.sugarguard.controller

import com.sugarguard.dto.RecommendationRequest
import com.sugarguard.dto.RecommendationResponse
import com.sugarguard.dto.RecommendationLogRequest
import com.sugarguard.dto.RecommendationLogResponse
import com.sugarguard.entity.RecommendationLog
import com.sugarguard.repository.RecommendationLogRepository
import com.sugarguard.service.RecommendationService
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/v1")
@CrossOrigin(origins = ["*"])
class RecommendationController(
    private val recommendationService: RecommendationService,
    // 💡 추가됨: 로깅 저장을 위한 Repository 의존성 주입
    private val logRepository: RecommendationLogRepository
) {
    // 1. 기존 활동 추천 판단 API
    @PostMapping("/recommendations")
    fun getRecommendation(@RequestBody request: RecommendationRequest): RecommendationResponse {
        return recommendationService.getRecommendation(request)
    }

    // 2. 수락/거절 로깅 API
    @PostMapping("/recommendations/log")
    fun saveRecommendationLog(@RequestBody request: RecommendationLogRequest): RecommendationLogResponse {
        val log = RecommendationLog(
            activityType = request.activityType,
            accepted = request.accepted
        )
        logRepository.save(log)

        val statusMsg = if (request.accepted) "수락" else "거절"
        return RecommendationLogResponse(
            success = true,
            message = "추천 활동($statusMsg) 기록이 성공적으로 저장되었습니다."
        )
    }
}