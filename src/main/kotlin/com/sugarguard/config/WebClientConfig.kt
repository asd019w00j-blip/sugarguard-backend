package com.sugarguard.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.client.reactive.ReactorClientHttpConnector
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.servlet.config.annotation.CorsRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer
import reactor.netty.http.client.HttpClient
import java.time.Duration

@Configuration
class WebClientConfig : WebMvcConfigurer {

    // 프론트엔드 CORS 허용 설정 추가
    override fun addCorsMappings(registry: CorsRegistry) {
        registry.addMapping("/**")
            .allowedOrigins(
                "http://localhost:3000", // 기존 로컬 테스트용
                "http://localhost:8080",
                "https://sugar-guard-frontend.vercel.app" // Vercel 프론트엔드 주소 허용
            )
            .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
            .allowCredentials(true)
    }

    // 기존 파이썬 AI 서버 통신용 설정
    @Bean
    fun webClient(): WebClient {
        val httpClient = HttpClient.create()
            .responseTimeout(Duration.ofSeconds(15)) // 15초로 변경

        return WebClient.builder()
            .clientConnector(ReactorClientHttpConnector(httpClient))
            .build()
    }
}