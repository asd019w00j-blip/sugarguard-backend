package com.sugarguard.entity

import jakarta.persistence.*
import java.time.LocalDateTime
import java.time.ZoneId

@Entity
class ActivityRecord(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    // 아래 세 줄에 = "", = 0, = 0.0 처럼 기본값을 추가했습니다.
    val activityType: String = "",
    val durationMinutes: Int = 0,
    val distanceKm: Double = 0.0,

    val createdAt: LocalDateTime = LocalDateTime.now(ZoneId.of("Asia/Seoul"))
)