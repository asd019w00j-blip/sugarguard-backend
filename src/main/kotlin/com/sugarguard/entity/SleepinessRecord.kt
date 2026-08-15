package com.sugarguard.entity

import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
class SleepinessRecord(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    val beforeSleepiness: Int = 0,
    var afterSleepiness: Int? = null, // 돌아오기 전까진 비어있음

    val createdAt: LocalDateTime = LocalDateTime.now()
)