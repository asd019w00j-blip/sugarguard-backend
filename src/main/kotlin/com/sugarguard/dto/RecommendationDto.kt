package com.sugarguard.dto

// 요청 DTO
data class RecommendationRequest(
    val latitude: Double,
    val longitude: Double
)

// 응답 DTO
data class RecommendationResponse(
    val success: Boolean,
    val environment: EnvironmentDto,
    val recommendation: RecommendationDataDto
)

data class EnvironmentDto(
    val retrievedAt: String,
    val dataSource: String,
    val weatherCondition: String,
    val temperature: Double,
    val feelsLikeTemperature: Double?,
    val pmGrade: String,
    val nearbyParks: List<ParkDto>
)

data class ParkDto(
    val name: String,
    val latitude: Double,
    val longitude: Double,
    val distanceMeters: Int
)

data class RecommendationDataDto(
    val activityId: String,
    val activityType: String, // OUTDOOR_WALK, INDOOR_STAIRS, INDOOR_SQUAT[cite: 2]
    val activityName: String,
    val durationMinutes: Int = 15,
    val reason: String,
    val location: LocationDto?,
    val guideText: String?
)

data class LocationDto(
    val name: String,
    val latitude: Double,
    val longitude: Double
)