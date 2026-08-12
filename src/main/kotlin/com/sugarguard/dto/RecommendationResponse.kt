package com.sugarguard.dto

data class RecommendationResponse(
    val success: Boolean = true,
    val environment: EnvironmentDto,
    val recommendation: RecommendationDto
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

data class RecommendationDto(
    val activityId: String,
    val activityType: String,
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