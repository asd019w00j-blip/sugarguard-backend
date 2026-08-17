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

            // 6초 초과 또는 503 에러 발생 시 앱이 멈추지 않고 아래 문구를 강제 반환
            "날씨가 좋네요! 가볍게 15분 동안 활동하며 리프레시해 볼까요?"
        }
    }

    fun generateResultMessage(request: LlmRequest): String {
        val prompt = if (request.contextType == "RESULT_COMPARISON") {

            val sleepinessMessage = when {
                request.sleepinessDiff!! > 0 -> {
                    "활동 전후로 졸림 수치가 ${request.sleepinessDiff}만큼 개선되었어."
                }

                request.sleepinessDiff == 0 -> {
                    "활동 전후로 졸림 수치에 변화가 없었어."
                }

                else -> {
                    "활동 전후로 졸림 수치가 ${kotlin.math.abs(request.sleepinessDiff)}만큼 높아졌어."
                }
            }

            """
            사용자가 방금 '${request.activityType}' 활동을 마치고 왔어.
            $sleepinessMessage
            이 결과에 맞게 사용자를 칭찬하거나 격려하고, 남은 하루도 파이팅하라는
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
                ?: "활동을 무사히 마치셨군요! 상쾌해진 기분으로 남은 하루도 활기차게 보내세요!"

        } catch (e: Exception) {
            println("제미나이 결과 문구 생성 API 에러: ${e.message}")

            "활동을 무사히 마치셨군요! 상쾌해진 기분으로 남은 하루도 활기차게 보내세요!"
        }
    }
}