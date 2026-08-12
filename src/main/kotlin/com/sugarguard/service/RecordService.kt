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
import kotlin.math.round

@Service
class RecordService(
    private val recordRepository: ActivityRecordRepository
) {
    // 1. 활동 완료 기록 저장
    fun saveRecord(request: RecordRequest): RecordResponse {
        val record = ActivityRecord(
            activityType = request.activityType,
            durationMinutes = request.durationMinutes,
            distanceKm = request.distanceKm
        )
        recordRepository.save(record) // DB에 저장!

        return RecordResponse(
            success = true,
            message = "활동 기록이 성공적으로 저장되었습니다.",
            earnedBadge = "첫 도전 배지 획득!" // MVP 단계에서는 고정 메시지 또는 간단한 조건문으로 처리
        )
    }

    // 2. 홈 화면 상태 조회 (진행바, 누적 거리, 연속 달성일)
    fun getHomeStatus(): HomeResponse {
        // 오늘 00:00:00 부터 23:59:59 까지의 시간 구하기
        val todayStart = LocalDateTime.of(LocalDate.now(), LocalTime.MIN)
        val todayEnd = LocalDateTime.of(LocalDate.now(), LocalTime.MAX)

        // Repository를 통해 오늘 기록이 1개라도 있는지 개수 확인
        val todayRecordCount = recordRepository.countByCreatedAtBetween(todayStart, todayEnd)
        val isCompletedToday = todayRecordCount > 0

        // DB에 있는 모든 기록을 가져와서 누적 거리와 활동 일수 계산 (MVP용 간략화 로직)
        val allRecords = recordRepository.findAll()
        val totalDistance = allRecords.sumOf { it.distanceKm }

        // 날짜만 추출해서 중복을 제거한 뒤 개수를 세면 대략적인 활동 일수가 나옵니다.
        val activeDays = allRecords.map { it.createdAt.toLocalDate() }.distinct().size

        return HomeResponse(
            success = true,
            data = HomeDataDto(
                isCompletedToday = isCompletedToday,
                continuousDays = activeDays,
                totalDistanceKm = round(totalDistance * 10) / 10.0 // 소수점 첫째 자리까지만 표시
            )
        )
    }
}