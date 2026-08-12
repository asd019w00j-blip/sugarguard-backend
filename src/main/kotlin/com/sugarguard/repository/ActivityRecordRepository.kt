package com.sugarguard.repository

import com.sugarguard.entity.ActivityRecord
import org.springframework.data.jpa.repository.JpaRepository
import java.time.LocalDateTime

interface ActivityRecordRepository : JpaRepository<ActivityRecord, Long> {
    // 특정 날짜(예: 오늘 0시 ~ 24시) 사이에 완료한 기록이 몇 개인지 세어주는 기능
    fun countByCreatedAtBetween(start: LocalDateTime, end: LocalDateTime): Long
}