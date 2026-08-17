package com.sugarguard.service

import com.sugarguard.dto.AirKoreaResponse
import com.sugarguard.dto.KmaWeatherResponse
import com.sugarguard.util.WeatherUtil
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClient
import java.net.URI
import java.net.URLEncoder
import java.time.Duration

@Service
class EnvironmentService(
    private val webClient: WebClient
) {
    @Value("\${public-data.service-key}")
    private lateinit var publicDataServiceKey: String

    fun getWeatherData(latitude: Double, longitude: Double): Pair<Double, Boolean> {
        return try {
            val (nx, ny) = WeatherUtil.convertGrid(latitude, longitude)
            val (baseDate, baseTime) = WeatherUtil.getBaseDateTime()

            // WebClient의 자동 인코딩을 막기 위해 URI 객체로 직접 생성
            val urlString = "http://apis.data.go.kr/1360000/VilageFcstInfoService_2.0/getUltraSrtNcst?serviceKey=$publicDataServiceKey&pageNo=1&numOfRows=1000&dataType=JSON&base_date=$baseDate&base_time=$baseTime&nx=$nx&ny=$ny"

            val weatherResponse = webClient.get()
                .uri(URI(urlString))
                .retrieve()
                .bodyToMono(KmaWeatherResponse::class.java)
                .timeout(Duration.ofSeconds(6))
                .block()

            val items = weatherResponse?.response?.body?.items?.item ?: emptyList()
            val temperatureStr = items.find { it.category == "T1H" }?.obsrValue ?: "25.0"
            val precipitationType = items.find { it.category == "PTY" }?.obsrValue ?: "0"

            val temperature = temperatureStr.toDoubleOrNull() ?: 25.0
            val isRaining = precipitationType != "0"

            Pair(temperature, isRaining)

        } catch (e: Exception) {
            println("기상청 API 6초 타임아웃 또는 연결 에러: ${e.message}")
            Pair(25.0, false)
        }
    }

    fun getFineDustGrade(): String {
        return try {
            // 한글 '충남' 파라미터가 깨지지 않도록 수동 인코딩
            val sidoEncoded = URLEncoder.encode("충남", "UTF-8")

            // WebClient의 자동 인코딩을 막기 위해 URI 객체로 직접 생성
            val urlString = "http://apis.data.go.kr/B552584/ArpltnInforInqireSvc/getCtprvnRltmMesureDnsty?serviceKey=$publicDataServiceKey&returnType=json&numOfRows=100&pageNo=1&sidoName=$sidoEncoded&ver=1.0"

            val response = webClient.get()
                .uri(URI(urlString))
                .retrieve()
                .bodyToMono(AirKoreaResponse::class.java)
                .timeout(Duration.ofSeconds(6))
                .block()

            val items = response?.response?.body?.items ?: emptyList()
            val firstValidItem = items.find { it.pm10Grade != null }
            val gradeValue = firstValidItem?.pm10Grade ?: "1"

            when (gradeValue) {
                "1" -> "GOOD"
                "2" -> "MODERATE"
                "3" -> "BAD"
                "4" -> "VERY_BAD"
                else -> "GOOD"
            }
        } catch (e: Exception) {
            println("미세먼지 API 6초 타임아웃 또는 연결 에러: ${e.message}")
            "MODERATE"
        }
    }
}