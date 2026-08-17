package com.sugarguard.dto

data class LlmRequest(
    val contextType: String,
    val activityType: String? = null,
    val sleepinessDiff: Int? = null
)

data class LlmResponse(
    val success: Boolean = true,
    val generatedText: String
)