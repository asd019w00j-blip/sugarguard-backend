package com.sugarguard.dto

// 나가기 전 (1차 탭)
data class SleepinessOutRequest(
    val beforeSleepiness: Int // 예: 1(아주 개운함) ~ 5(매우 졸림)
)

data class SleepinessOutResponse(
    val recordId: Long,
    val message: String
)

// 돌아와서 (2차 탭)
data class SleepinessInRequest(
    val recordId: Long,
    val afterSleepiness: Int
)

data class SleepinessInResponse(
    val recordId: Long,
    val beforeSleepiness: Int,
    val afterSleepiness: Int,
    val difference: Int, // 졸림 개선도 (before - after)
    val message: String
)