package com.sugarguard.dto

// 프론트엔드가 백엔드로 보내는 데이터 (Request)
data class RecordRequest(
    val activityType: String,
    val durationMinutes: Int,
    val distanceKm: Double
)

// 백엔드가 프론트엔드에게 돌려주는 데이터 (Response)
data class RecordResponse(
    val success: Boolean,
    val message: String,
    val earnedBadge: String? = null, // 획득한 배지가 없으면 null 반환
    val continuousDays: Int = 0,
    val totalDistanceKm: Double = 0.0
)