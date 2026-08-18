package com.sugarguard.controller

import com.sugarguard.dto.*
import com.sugarguard.service.SleepinessService
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/v1/sleepiness")

class SleepinessController(
    private val sleepinessService: SleepinessService
) {
    @PostMapping("/out")
    fun recordOut(@RequestBody request: SleepinessOutRequest): SleepinessOutResponse {
        return sleepinessService.recordOut(request)
    }

    @PostMapping("/in")
    fun recordIn(@RequestBody request: SleepinessInRequest): SleepinessInResponse {
        return sleepinessService.recordIn(request)
    }
}