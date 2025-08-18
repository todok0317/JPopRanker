package com.example.jpopranker.crawler.controller;

import com.example.jpopranker.crawler.service.CrawlerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/crawler")
@RequiredArgsConstructor
@Tag(name = "Crawler", description = "음악 차트 크롤링 API")
public class CrawlerController {
    private final CrawlerService crawlerService;

    @Operation(summary = "테스트 크롤링", description = "샘플 데이터로 크롤링 기능을 테스트합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "크롤링 성공"),
            @ApiResponse(responseCode = "500", description = "크롤링 실패")
    })
    @PostMapping("/test")
    public String testCrawl() {
        log.info("테스트 크롤링 시작");
        crawlerService.crawlTestData();
        return "테스트 크롤링 완료! 데이터를 확인해보세요.";
    }

    @Operation(summary = "Billboard Japan 크롤링", description = "Billboard Japan Hot 100 차트를 크롤링합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "크롤링 성공"),
            @ApiResponse(responseCode = "500", description = "크롤링 실패")
    })
    @PostMapping("/billboard-japan")
    public String crawlBillboardJapan() {
        log.info("Billboard Japan 크롤링 시작");
        crawlerService.crawlBillboardJapan();
        return "Billboard Japan 크롤링 완료!";
    }

    @Operation(summary = "Oricon 크롤링", description = "Oricon 주간 합산 싱글 차트를 크롤링합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "크롤링 성공"),
            @ApiResponse(responseCode = "500", description = "크롤링 실패")
    })
    @PostMapping("/oricon")
    public String crawlOricon() {
        log.info("Oricon 크롤링 시작");
        crawlerService.crawlOriconChart();
        return "Oricon 크롤링 완료!";
    }

    @Operation(summary = "전체 차트 크롤링", description = "모든 지원되는 차트를 한번에 크롤링합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "전체 크롤링 성공"),
            @ApiResponse(responseCode = "500", description = "크롤링 실패")
    })
    @PostMapping("/all")
    public String crawlAllCharts() {
        log.info("전체 차트 크롤링 시작");
        crawlerService.crawlBillboardJapan();
        crawlerService.crawlOriconChart();
        return "전체 차트 크롤링 완료!";
    }

    @Operation(summary = "크롤링 서비스 상태 확인", description = "크롤링 서비스가 정상 작동하는지 확인합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "서비스 정상")
    })
    @GetMapping("/status")
    public String getStatus() {
        return "크롤링 서비스가 정상 작동 중입니다.";
    }
}
