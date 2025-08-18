package com.example.jpopranker.crawler.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration // 스프링 설정 클래스
// 스프링이 시작할 때 이 클래스를 읽어서 Bean들을 등록함
public class CrawlerConfig {

    @Bean // 스프링 컨테이너가 관리할 객체를 만드는 메서드
    // WebClient는 HTTP 요청을 보내는 도구
    // 웹사이트에 접속하여 HTML을 가져올 때 사용
    // Jsoup.connect() 대신 더 고급 기능 제공
    public WebClient webClient() {
        return WebClient.builder()
            // User-Agent는 브라우저 정보를 속이는 헤더
            .defaultHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")
            .build();
    }


}
