package com.sugarguard.service

import com.sugarguard.dto.AirKoreaResponse
import com.sugarguard.dto.KmaWeatherResponse
import com.sugarguard.util.WeatherUtil
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClient
import java.time.Duration // 타임아웃 설정을 위해 추가

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

            val weatherResponse = webClient.get()
                .uri { uriBuilder ->
                    uriBuilder.scheme("http").host("apis.data.go.kr").path("/1360000/VilageFcstInfoService_2.0/getUltraSrtNcst")
                        .queryParam("serviceKey", publicDataServiceKey)
                        .queryParam("pageNo", "1").queryParam("numOfRows", "1000").queryParam("dataType", "JSON")
                        .queryParam("base_date", baseDate).queryParam("base_time", baseTime)
                        .queryParam("nx", nx).queryParam("ny", ny).build()
                }
                .retrieve()
                .bodyToMono(KmaWeatherResponse::class.java)
                .timeout(Duration.ofSeconds(8)) // 8초 타임아웃 실제 적용
                .block()

            val items = weatherResponse?.response?.body?.items?.item ?: emptyList()
            val temperatureStr = items.find { it.category == "T1H" }?.obsrValue ?: "25.0"
            val precipitationType = items.find { it.category == "PTY" }?.obsrValue ?: "0"

            val temperature = temperatureStr.toDoubleOrNull() ?: 25.0
            val isRaining = precipitationType != "0"

            Pair(temperature, isRaining)

        } catch (e: Exception) {
            // 타임아웃 발생 시 콘솔창에 원인을 명확히 남깁니다.
            println("기상청 API 8초 타임아웃 또는 연결 에러: ${e.message}")
            // 에러 발생 시 개발자님이 설정해 둔 기본 대체 데이터 반환
            Pair(25.0, false)
        }
    }

    fun getFineDustGrade(): String {
        return try {
            val response = webClient.get()
                .uri { uriBuilder ->
                    uriBuilder
                        .scheme("http")
                        .host("apis.data.go.kr")
                        .path("/B552584/ArpltnInforInqireSvc/getCtprvnRltmMesureDnsty")
                        .queryParam("serviceKey", publicDataServiceKey)
                        .queryParam("returnType", "json")
                        .queryParam("numOfRows", "100")
                        .queryParam("pageNo", "1")
                        .queryParam("sidoName", "충남")
                        .queryParam("ver", "1.0")
                        .build()
                }
                .retrieve()
                .bodyToMono(AirKoreaResponse::class.java)
                .timeout(Duration.ofSeconds(8)) // 8초 타임아웃 실제 적용
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
            // 타임아웃 발생 시 로그를 남기고 기본값 반환
            println("미세먼지 API 8초 타임아웃 또는 연결 에러: ${e.message}")
            "MODERATE"
        }
    }
}