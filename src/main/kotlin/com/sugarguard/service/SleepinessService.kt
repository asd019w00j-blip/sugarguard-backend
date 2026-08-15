package com.sugarguard.service

import com.sugarguard.dto.*
import com.sugarguard.entity.SleepinessRecord
import com.sugarguard.repository.SleepinessRecordRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class SleepinessService(
    private val sleepinessRecordRepository: SleepinessRecordRepository
) {
    // 1. 나가기 전 탭: 기록 방 생성
    @Transactional
    fun recordOut(request: SleepinessOutRequest): SleepinessOutResponse {
        val record = SleepinessRecord(beforeSleepiness = request.beforeSleepiness)
        val savedRecord = sleepinessRecordRepository.save(record)

        return SleepinessOutResponse(
            recordId = savedRecord.id!!,
            message = "외출 전 졸림 수치가 기록되었습니다. 다녀오세요!"
        )
    }

    // 2. 돌아와서 탭: 결과 비교 및 업데이트
    @Transactional
    fun recordIn(request: SleepinessInRequest): SleepinessInResponse {
        val record = sleepinessRecordRepository.findById(request.recordId)
            .orElseThrow { IllegalArgumentException("해당 기록을 찾을 수 없습니다.") }

        record.afterSleepiness = request.afterSleepiness
        sleepinessRecordRepository.save(record)

        // 비교값 산출 (양수일수록 졸림이 많이 깼다는 의미)
        val diff = record.beforeSleepiness - request.afterSleepiness

        return SleepinessInResponse(
            recordId = record.id!!,
            beforeSleepiness = record.beforeSleepiness,
            afterSleepiness = request.afterSleepiness,
            difference = diff,
            message = "산책 후 졸림 수치가 ${diff}만큼 개선되었습니다!"
        )
    }
}