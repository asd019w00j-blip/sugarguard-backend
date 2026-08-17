package com.sugarguard.service

import com.sugarguard.dto.*
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClient
import java.time.Duration
import kotlin.math.abs

@Service
class GeminiService(
    private val webClient: WebClient
) {
    @Value("\${gemini.api-key}")
    private lateinit var geminiApiKey: String

    // 기존 메서드: 활동 추천 멘트 생성
    fun generateCoachingText(
        weatherCondition: String,
        pmGrade: String,
        activityName: String
    ): String {
        val prompt = """
            너는 사용자의 건강과 일상을 챙겨주는 다정하고 파이팅 넘치는 퍼스널 트레이너야.
            현재 날씨는 '$weatherCondition'이고, 미세먼지 등급은 '$pmGrade'야.
            그래서 사용자에게 '$activityName' 활동을 15분 동안 하라고 추천하려고 해.

            사용자가 기분 좋게 활동을 시작할 수 있도록, 날씨 상황을 자연스럽게 언급하면서
            추천 활동을 독려하는 코칭 멘트를 딱 1문장(최대 50자 이내)으로 만들어줘.
        """.trimIndent()

        val requestBody = GeminiRequest(
            contents = listOf(
                GeminiContent(
                    parts = listOf(
                        GeminiPart(text = prompt)
                    )
                )
            )
        )

        return try {
            val response = webClient.post()
                .uri(
                    "https://generativelanguage.googleapis.com/v1beta/models/gemini-flash-latest:generateContent?key=$geminiApiKey"
                )
                .bodyValue(requestBody)
                .retrieve()
                .bodyToMono(GeminiResponse::class.java)
                .timeout(Duration.ofSeconds(15))
                .block()

            response?.candidates
                ?.firstOrNull()
                ?.content
                ?.parts
                ?.firstOrNull()
                ?.text
                ?: "날씨가 좋네요! 가볍게 15분 동안 활동하며 리프레시해 볼까요?"

        } catch (e: Exception) {
            println("제미나이 API 6초 타임아웃 또는 에러: ${e.message}")
            "날씨가 좋네요! 가볍게 15분 동안 활동하며 리프레시해 볼까요?"
        }
    }

    // 결과 화면 멘트 생성
    fun generateResultMessage(request: LlmRequest): String {
        // Null 방지 및 절댓값 미리 계산
        val diff = request.sleepinessDiff ?: 0
        val absDiff = abs(diff)

        val prompt = if (request.contextType == "RESULT_COMPARISON") {

            //  1. 현재 졸림 상태에 대한 정확한 묘사
            val sleepinessMessage = when {
                diff > 0 -> "활동 전보다 졸림 수치가 ${absDiff}만큼 개선되어서 훨씬 상쾌해졌어."
                diff == 0 -> "활동 전후로 졸림 수치에 변화가 없었어."
                else -> "오히려 활동 전보다 졸림 수치가 ${absDiff}만큼 높아져서 피곤해진 상태야."
            }

            //  2. 상황에 맞는 제미나이의 말투 지정
            val toneMessage = when {
                diff > 0 -> "이 긍정적인 변화를 칭찬하고 남은 하루도 파이팅하라는"
                diff == 0 -> "변화는 없지만 활동을 완료한 것 자체를 격려하고 남은 하루도 잘 보내라는"
                else -> "무리하지 말고 잠시 쉬어가라고 따뜻하게 위로하고 격려하는"
            }

            """
            사용자가 방금 '${request.activityType}' 활동을 마치고 왔어.
            $sleepinessMessage
            이 결과에 맞게 $toneMessage 
            다정한 코칭 멘트를 딱 1문장(최대 50자 이내)으로 만들어줘.
            """.trimIndent()

        } else {
            "사용자에게 건네는 다정하고 활기찬 인사말을 딱 1문장(최대 50자)으로 작성해줘."
        }

        val requestBody = GeminiRequest(
            contents = listOf(
                GeminiContent(
                    parts = listOf(
                        GeminiPart(text = prompt)
                    )
                )
            )
        )

        return try {
            val response = webClient.post()
                .uri("https://generativelanguage.googleapis.com/v1beta/models/gemini-flash-latest:generateContent?key=$geminiApiKey")
                .bodyValue(requestBody)
                .retrieve()
                .bodyToMono(GeminiResponse::class.java)
                .timeout(Duration.ofSeconds(15))
                .block()

            response?.candidates
                ?.firstOrNull()
                ?.content
                ?.parts
                ?.firstOrNull()
                ?.text
                ?: "활동을 무사히 마치셨군요! 남은 하루도 파이팅입니다!"

        } catch (e: Exception) {
            println("제미나이 결과 문구 생성 API 에러: ${e.message}")
            "활동을 무사히 마치셨군요! 남은 하루도 파이팅입니다!"
        }
    }
}