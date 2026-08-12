package com.sugarguard.controller

import com.sugarguard.dto.*
import com.sugarguard.service.RecommendationService
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/v1")
@CrossOrigin(origins = ["*"])
class RecommendationController(
    private val recommendationService: RecommendationService
) {

    @PostMapping("/recommendations")
    fun getRecommendation(@RequestBody request: RecommendationRequest): RecommendationResponse {
        // 가짜 데이터를 지우고, Service의 메서드를 호출하여 결과를 반환
        return recommendationService.createRecommendation(request)
    }
}