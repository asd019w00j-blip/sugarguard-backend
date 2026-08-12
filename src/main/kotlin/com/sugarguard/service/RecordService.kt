package com.sugarguard.service

import com.sugarguard.dto.HomeDataDto
import com.sugarguard.dto.HomeResponse
import com.sugarguard.dto.RecordRequest
import com.sugarguard.dto.RecordResponse
import com.sugarguard.entity.ActivityRecord
import com.sugarguard.repository.ActivityRecordRepository
import org.springframework.stereotype.Service
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import kotlin.math.round

@Service
class RecordService(
    private val recordRepository: ActivityRecordRepository
) {
    // 💡 배포 서버 환경과 무관하게 무조건 한국 시간(KST)으로 기준을 잡습니다.
    private val seoulZoneId = ZoneId.of("Asia/Seoul")

    // 1. 활동 완료 기록 저장
    fun saveRecord(request: RecordRequest): RecordResponse {
        val record = ActivityRecord(
            activityType = request.activityType,
            durationMinutes = request.durationMinutes,
            distanceKm = request.distanceKm,
            // 저장할 때도 한국 시간 기준으로 명시하여 저장합니다.
            createdAt = LocalDateTime.now(seoulZoneId)
        )
        recordRepository.save(record)

        return RecordResponse(
            success = true,
            message = "활동 기록이 성공적으로 저장되었습니다.",
            earnedBadge = "첫 도전 배지 획득!"
        )
    }

    // 2. 홈 화면 상태 조회 (진행바, 누적 거리, 연속 달성일)
    fun getHomeStatus(): HomeResponse {
        // 한국 시간 기준의 '오늘'
        val today = LocalDate.now(seoulZoneId)
        val todayStart = LocalDateTime.of(today, LocalTime.MIN)
        val todayEnd = LocalDateTime.of(today, LocalTime.MAX)

        val todayRecordCount = recordRepository.countByCreatedAtBetween(todayStart, todayEnd)
        val isCompletedToday = todayRecordCount > 0

        val allRecords = recordRepository.findAll()
        val totalDistance = allRecords.sumOf { it.distanceKm }

        // [수정됨] 실제 '연속' 달성일 계산 로직
        val distinctDates = allRecords
            .map { it.createdAt.toLocalDate() }
            .distinct()
            .sortedDescending() // 최신 날짜부터 내림차순 정렬

        var continuousDays = 0
        if (distinctDates.isNotEmpty()) {
            val latestDate = distinctDates.first()

            // 가장 최근 활동이 '오늘'이거나 '어제'인 경우에만 연속 기록으로 인정
            if (latestDate == today || latestDate == today.minusDays(1)) {
                var expectedDate = latestDate
                for (date in distinctDates) {
                    if (date == expectedDate) {
                        continuousDays++
                        expectedDate = expectedDate.minusDays(1) // 하루씩 과거로 거슬러 올라감
                    } else {
                        break // 중간에 하루라도 비어있으면 루프 종료 (연속 끊김)
                    }
                }
            }
        }

        return HomeResponse(
            success = true,
            data = HomeDataDto(
                isCompletedToday = isCompletedToday,
                continuousDays = continuousDays,
                totalDistanceKm = round(totalDistance * 10) / 10.0
            )
        )
    }
}