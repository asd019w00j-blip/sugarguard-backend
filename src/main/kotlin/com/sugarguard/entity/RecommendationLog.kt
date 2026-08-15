package com.sugarguard.entity

import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
class RecommendationLog(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    val activityType: String = "",
    val accepted: Boolean = false,

    val createdAt: LocalDateTime = LocalDateTime.now()
)