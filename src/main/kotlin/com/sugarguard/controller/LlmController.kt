package com.sugarguard.controller

import com.sugarguard.dto.LlmRequest
import com.sugarguard.dto.LlmResponse
import com.sugarguard.service.GeminiService
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/v1/llm")
@CrossOrigin(origins = ["*"])
class LlmController(
    private val geminiService: GeminiService
) {
    @PostMapping("/generate-message")
    fun generateMessage(@RequestBody request: LlmRequest): LlmResponse {
        val generatedText = geminiService.generateResultMessage(request)
        return LlmResponse(generatedText = generatedText)
    }
}