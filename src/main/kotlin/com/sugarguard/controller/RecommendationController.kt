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
// 💡 @CrossOrigin 어노테이션 삭제됨
class RecommendationController(
    private val recommendationService: RecommendationService,
    private val logRepository: RecommendationLogRepository
) {
    @PostMapping("/recommendations")
    fun getRecommendation(@RequestBody request: RecommendationRequest): RecommendationResponse {
        return recommendationService.getRecommendation(request)
    }

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