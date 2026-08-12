package com.sugarguard.dto

data class AirKoreaResponse(val response: AirKoreaBody)
data class AirKoreaBody(val header: AirKoreaHeader, val body: AirKoreaItems?)
data class AirKoreaHeader(val resultCode: String, val resultMsg: String)
data class AirKoreaItems(val items: List<AirKoreaItem>?)

data class AirKoreaItem(
    val stationName: String, // 측정소 이름
    val pm10Grade: String?,  // 미세먼지 등급 (1:좋음, 2:보통, 3:나쁨, 4:매우나쁨)
    val pm25Grade: String?   // 초미세먼지 등급
)