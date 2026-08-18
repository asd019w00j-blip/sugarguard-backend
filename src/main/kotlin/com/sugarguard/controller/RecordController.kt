package com.sugarguard.controller

import com.sugarguard.dto.HomeResponse
import com.sugarguard.dto.RecordRequest
import com.sugarguard.dto.RecordResponse
import com.sugarguard.service.RecordService
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/v1")

class RecordController(
    private val recordService: RecordService
) {
    @GetMapping("/home")
    fun getHomeStatus(): HomeResponse {
        return recordService.getHomeStatus()
    }

    @PostMapping("/records")
    fun saveRecord(@RequestBody request: RecordRequest): RecordResponse {
        return recordService.saveRecord(request)
    }
}