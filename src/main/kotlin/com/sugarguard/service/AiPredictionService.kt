package com.sugarguard.service

import com.sugarguard.dto.AiPredictionRequest
import com.sugarguard.dto.AiPredictionResponse
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClient
import java.time.Duration

@Service
class AiPredictionService(
    private val webClient: WebClient
) {
    fun getCompletionPrediction(request: AiPredictionRequest): Int {
        return try {
            val response = webClient.post()
                // 파이썬 FastAPI 서버 주소로 요청을 보냅니다.
                .uri("http://localhost:8000/api/ai/predict")
                .bodyValue(request)
                .retrieve()
                .bodyToMono(AiPredictionResponse::class.java)
                .timeout(Duration.ofSeconds(3)) // AI 응답 3초 대기
                .block()

            // AI가 예측한 결과 (1: 완료 예상, 0: 포기 예상) 반환
            response?.prediction ?: 1

        } catch (e: Exception) {
            println("AI 서버 연결 실패 또는 타임아웃: ${e.message}")
            // 에러 시 앱이 멈추지 않도록 기본적으로 긍정(1) 반환
            1
        }
    }
}