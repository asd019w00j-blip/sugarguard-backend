package com.sugarguard.repository

import com.sugarguard.entity.RecommendationLog
import org.springframework.data.jpa.repository.JpaRepository

interface RecommendationLogRepository : JpaRepository<RecommendationLog, Long>