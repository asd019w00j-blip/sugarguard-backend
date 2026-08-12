package com.sugarguard.dto

data class HomeResponse(
    val success: Boolean,
    val data: HomeDataDto
)

data class HomeDataDto(
    val isCompletedToday: Boolean,
    val continuousDays: Int,
    val totalDistanceKm: Double
)