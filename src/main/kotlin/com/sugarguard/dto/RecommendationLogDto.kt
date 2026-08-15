package com.sugarguard.dto

data class RecommendationLogRequest(
    val activityType: String, // 예: "OUTDOOR_WALK", "INDOOR_SQUAT"
    val accepted: Boolean     // true: 수락, false: 거절/포기
)

data class RecommendationLogResponse(
    val success: Boolean,
    val message: String
)