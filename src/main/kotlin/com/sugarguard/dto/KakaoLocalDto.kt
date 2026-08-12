package com.sugarguard.dto

data class KakaoLocalResponse(
    val documents: List<KakaoDocument>
)

data class KakaoDocument(
    val place_name: String,
    val x: String,
    val y: String,
    val distance: String
)