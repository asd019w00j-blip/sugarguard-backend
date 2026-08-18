package com.sugarguard.service

import com.sugarguard.dto.AiPredictionRequest
import com.sugarguard.dto.AiPredictionResponse
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClient
import java.time.Duration

@Service
class AiPredictionService(
    private val webClient: WebClient
) {
    // application.yaml을 통해 환경 변수(AI_SERVER_URL) 값을 가져옴
    @Value("\${ai.server-url}")
    private lateinit var aiServerUrl: String

    fun getCompletionPrediction(request: AiPredictionRequest): Int {
        return try {
            val response = webClient.post()
                // 고정된 localhost 주소 대신 변수를 조합하여 사용
                .uri("$aiServerUrl/api/ai/predict")
                .bodyValue(request)
                .retrieve()
                .bodyToMono(AiPredictionResponse::class.java)
                .timeout(Duration.ofSeconds(3))
                .block()

            response?.prediction ?: 1

        } catch (e: Exception) {
            println("AI 서버 연결 실패 또는 타임아웃: ${e.message}")
            1
        }
    }
}