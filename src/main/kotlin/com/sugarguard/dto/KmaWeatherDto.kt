package com.sugarguard.dto

// 기상청 JSON 전체 구조 매핑
data class KmaWeatherResponse(val response: KmaBody)
data class KmaBody(val header: KmaHeader, val body: KmaItems?)
data class KmaHeader(val resultCode: String, val resultMsg: String)
data class KmaItems(val items: KmaItemList)
data class KmaItemList(val item: List<KmaItem>)

// 실제 온도, 강수 형태 등이 들어있는 핵심 알맹이
data class KmaItem(
    val category: String, // 예: "T1H"(기온), "PTY"(강수형태)
    val obsrValue: String // 예: "27.5", "0"
)