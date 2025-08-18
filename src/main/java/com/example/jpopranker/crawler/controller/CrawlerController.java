package com.example.jpopranker.crawler.controller;

import com.example.jpopranker.crawler.service.CrawlerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/crawler")
@RequiredArgsConstructor
public class CrawlerController {
    private final CrawlerService crawlerService;

    // 테스트용 크롤링
    @PostMapping("/test")
    public String testCrawl() {
        log.info("테스트 크롤링 시작");
        crawlerService.crawlTestData();
        return "테스트 크롤링 완료! 데이터를 확인해보세요.";
    }

    // 실제 사이트 크롤링 (나중에 구현)
    @PostMapping("/billboard-japan")
    public String crawlBillboardJapan() {
        log.info("Billboard Japan 크롤링 시작");
        crawlerService.crawlBillboardJapan();
        return "Billboard Japan 크롤링 완료!";
    }

    // Oricon 크롤링
    @PostMapping("/oricon")
    public String crawlOricon() {
        log.info("Oricon 크롤링 시작");
        crawlerService.crawlOriconChart();
        return "Oricon 크롤링 완료!";
    }

    // 모든 차트를 한번에 크롤링
    @PostMapping("/all")
    public String crawlAllCharts() {
        log.info("전체 차트 크롤링 시작");
        crawlerService.crawlBillboardJapan();
        crawlerService.crawlOriconChart();
        return "전체 차트 크롤링 완료!";
    }

    // 크롤링 상태 확인
    @GetMapping("/status")
    public String getStatus() {
        return "크롤링 서비스가 정상 작동 중입니다.";
    }
    
    
}
