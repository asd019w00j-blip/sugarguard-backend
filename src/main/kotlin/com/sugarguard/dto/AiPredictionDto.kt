package com.sugarguard.dto

// 파이썬 서버로 보낼 예측 요청 데이터
data class AiPredictionRequest(
    val temperature: Double,
    val pm_grade: Int,
    val is_raining: Int,
    val activity_type: Int,
    val user_walk_rate: Double,
    val user_stairs_rate: Double,
    val user_squat_rate: Double
)

// 파이썬 서버로부터 받을 예측 결과
data class AiPredictionResponse(
    val success: Boolean,
    val prediction: Int,
    val result_message: String
)