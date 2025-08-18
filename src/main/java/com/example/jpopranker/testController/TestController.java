package com.example.jpopranker.testController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Tag(name = "System", description = "시스템 상태 확인 API")
public class TestController {

    @Operation(summary = "홈페이지", description = "API 서비스가 정상 작동하는지 확인합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "서비스 정상")
    })
    @GetMapping("/")
    public String home() {
        return "JPop Ranker API is running!";
    }

    @Operation(summary = "테스트 엔드포인트", description = "기본적인 API 연결을 테스트합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "테스트 성공")
    })
    @GetMapping("/test")
    public String test() {
        return "Test endpoint works!";
    }

    @Operation(summary = "헬스 체크", description = "서버 상태를 확인합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "서버 정상")
    })
    @GetMapping("/health")
    public String health() {
        return "Server is healthy!";
    }
}
