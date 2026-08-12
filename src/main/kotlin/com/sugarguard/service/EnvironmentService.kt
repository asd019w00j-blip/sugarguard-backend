package com.sugarguard.service

import com.sugarguard.dto.AirKoreaResponse
import com.sugarguard.dto.KmaWeatherResponse
import com.sugarguard.util.WeatherUtil
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClient

@Service
class EnvironmentService(
    private val webClient: WebClient
) {
    @Value("\${public-data.service-key}")
    private lateinit var publicDataServiceKey: String

    // 반환 타입을 Pair<Double, Boolean> (기온, 비오는지여부)로 지정합니다.
    fun getWeatherData(latitude: Double, longitude: Double): Pair<Double, Boolean> {
        return try {
            val (nx, ny) = WeatherUtil.convertGrid(latitude, longitude)
            val (baseDate, baseTime) = WeatherUtil.getBaseDateTime()

            val weatherResponse = webClient.get()
                // ... (이전과 동일한 uriBuilder 부분) ...
                .uri { uriBuilder ->
                    uriBuilder.scheme("http").host("apis.data.go.kr").path("/1360000/VilageFcstInfoService_2.0/getUltraSrtNcst")
                        .queryParam("serviceKey", publicDataServiceKey)
                        .queryParam("pageNo", "1").queryParam("numOfRows", "1000").queryParam("dataType", "JSON")
                        .queryParam("base_date", baseDate).queryParam("base_time", baseTime)
                        .queryParam("nx", nx).queryParam("ny", ny).build()
                }
                .retrieve()
                .bodyToMono(KmaWeatherResponse::class.java)
                .block()

            val items = weatherResponse?.response?.body?.items?.item ?: emptyList()
            val temperatureStr = items.find { it.category == "T1H" }?.obsrValue ?: "25.0"
            val precipitationType = items.find { it.category == "PTY" }?.obsrValue ?: "0"

            val temperature = temperatureStr.toDoubleOrNull() ?: 25.0
            val isRaining = precipitationType != "0" // 0이 아니면 비나 눈이 오는 상태

            Pair(temperature, isRaining)

        } catch (e: Exception) {
            println("기상청 API 호출 에러: ${e.message}")
            // 에러 발생 시 기본값 반환 (온도 25도, 맑음)
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
                        .queryParam("sidoName", "충남") // 충남 지역의 모든 측정소 데이터 조회
                        .queryParam("ver", "1.0")
                        .build()
                }
                .retrieve()
                .bodyToMono(AirKoreaResponse::class.java)
                .block()

            val items = response?.response?.body?.items ?: emptyList()

            // 데이터가 있는 첫 번째 측정소의 미세먼지 등급을 사용 (1:좋음, 2:보통, 3:나쁨, 4:매우나쁨)
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
            println("미세먼지 API 호출 에러: ${e.message}")
            "MODERATE" // 에러 시 기본값
        }
    }

}