package com.sugarguard.service

import com.sugarguard.dto.*
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClient
import java.time.Duration

@Service
class GeminiService(
    private val webClient: WebClient
) {
    @Value("\${gemini.api-key}")
    private lateinit var geminiApiKey: String

    fun generateCoachingText(weatherCondition: String, pmGrade: String, activityName: String): String {
        // 1. 제미나이에게 내릴 프롬프트(명령어) 작성
        val prompt = """
            너는 사용자의 건강과 일상을 챙겨주는 다정하고 파이팅 넘치는 퍼스널 트레이너야.
            현재 날씨는 '$weatherCondition'이고, 미세먼지 등급은 '$pmGrade'야.
            그래서 사용자에게 '$activityName' 활동을 15분 동안 하라고 추천하려고 해.
            
            사용자가 기분 좋게 활동을 시작할 수 있도록, 날씨 상황을 자연스럽게 언급하면서 
            추천 활동을 독려하는 코칭 멘트를 딱 1문장(최대 50자 이내)으로 만들어줘.
            이모지도 하나 적절하게 섞어줘.
        """.trimIndent()

        // 2. DTO 포장
        val requestBody = GeminiRequest(
            contents = listOf(
                GeminiContent(parts = listOf(GeminiPart(text = prompt)))
            )
        )

        return try {
            // 3. 제미나이 API (gemini-1.5-flash 모델) 호출
            val response = webClient.post()
                .uri("https://generativelanguage.googleapis.com/v1beta/models/gemini-flash-latest:generateContent?key=$geminiApiKey")
                .bodyValue(requestBody)
                .retrieve()
                .bodyToMono(GeminiResponse::class.java)
                .timeout(Duration.ofSeconds(15)) // 제미나이 응답 15초까지만 기다림
                .block()

            // 4. 응답 텍스트 파싱
            response?.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
                ?: "오늘도 힘차게 15분 건강을 챙겨볼까요? 화이팅!" // 파싱 실패 시 기본 멘트

        } catch (e: Exception) {
            println("제미나이 API 호출 에러: ${e.message}")
            "오늘도 힘차게 15분 건강을 챙겨볼까요? 화이팅!" // 에러 발생 시 기본 멘트 반환
        }
    }
}