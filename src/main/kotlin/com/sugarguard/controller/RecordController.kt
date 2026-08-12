package com.sugarguard.controller

import com.sugarguard.dto.HomeResponse
import com.sugarguard.dto.RecordRequest
import com.sugarguard.dto.RecordResponse
import com.sugarguard.service.RecordService
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/v1")
@CrossOrigin(origins = ["*"]) // 프론트엔드 로컬 연동을 위한 CORS 허용
class RecordController(
    private val recordService: RecordService
) {

    // 홈 화면 상태 조회 API[cite: 2]
    @GetMapping("/home")
    fun getHomeStatus(): HomeResponse {
        return recordService.getHomeStatus()
    }

    // 활동 완료 기록 저장 API[cite: 2]
    @PostMapping("/records")
    fun saveRecord(@RequestBody request: RecordRequest): RecordResponse {
        return recordService.saveRecord(request)
    }
}