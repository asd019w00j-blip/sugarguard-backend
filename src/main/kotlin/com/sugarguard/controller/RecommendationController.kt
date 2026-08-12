package com.sugarguard.controller

import com.sugarguard.dto.RecommendationRequest
import com.sugarguard.dto.RecommendationResponse
import com.sugarguard.service.RecommendationService
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/v1")
@CrossOrigin(origins = ["*"]) // 로컬 연동을 위한 CORS 허용
class RecommendationController(
    private val recommendationService: RecommendationService
) {
    // 활동 추천 판단 API[cite: 2]
    @PostMapping("/recommendations")
    fun getRecommendation(@RequestBody request: RecommendationRequest): RecommendationResponse {
        return recommendationService.getRecommendation(request)
    }
}