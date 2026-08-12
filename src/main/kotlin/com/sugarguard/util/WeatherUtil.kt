package com.sugarguard.util

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import kotlin.math.*

object WeatherUtil {
    // 1. 위경도 -> 기상청 격자 좌표(X, Y) 변환 공식
    fun convertGrid(lat: Double, lon: Double): Pair<Int, Int> {
        val RE = 6371.00877 // 지구 반경(km)
        val GRID = 5.0      // 격자 간격(km)
        val SLAT1 = 30.0    // 투영 위도1(degree)
        val SLAT2 = 60.0    // 투영 위도2(degree)
        val OLON = 126.0    // 기준점 경도(degree)
        val OLAT = 38.0     // 기준점 위도(degree)
        val XO = 43         // 기준점 X좌표(GRID)
        val YO = 136        // 기준점 Y좌표(GRID)

        val DEGRAD = Math.PI / 180.0
        val re = RE / GRID
        val slat1 = SLAT1 * DEGRAD
        val slat2 = SLAT2 * DEGRAD
        val olon = OLON * DEGRAD
        val olat = OLAT * DEGRAD

        var sn = tan(Math.PI * 0.25 + slat2 * 0.5) / tan(Math.PI * 0.25 + slat1 * 0.5)
        sn = ln(cos(slat1) / cos(slat2)) / ln(sn)
        var sf = tan(Math.PI * 0.25 + slat1 * 0.5)
        sf = sf.pow(sn) * cos(slat1) / sn
        var ro = tan(Math.PI * 0.25 + olat * 0.5)
        ro = re * sf / ro.pow(sn)

        var ra = tan(Math.PI * 0.25 + lat * DEGRAD * 0.5)
        ra = re * sf / ra.pow(sn)
        var theta = lon * DEGRAD - olon
        if (theta > Math.PI) theta -= 2.0 * Math.PI
        if (theta < -Math.PI) theta += 2.0 * Math.PI
        theta *= sn

        val x = (ra * sin(theta) + XO + 0.5).toInt()
        val y = (ro - ra * cos(theta) + YO + 0.5).toInt()
        return Pair(x, y)
    }

    // 2. 동적 시간 계산: 매시간 40분쯤 데이터가 나오므로 안전하게 1시간 전 데이터를 요청합니다.
    fun getBaseDateTime(): Pair<String, String> {
        val targetTime = LocalDateTime.now().minusHours(1)
        val baseDate = targetTime.format(DateTimeFormatter.ofPattern("yyyyMMdd"))
        val baseTime = targetTime.format(DateTimeFormatter.ofPattern("HH00"))

        return Pair(baseDate, baseTime)
    }
}