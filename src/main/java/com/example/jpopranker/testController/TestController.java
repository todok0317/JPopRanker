package com.example.jpopranker.testController;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TestController {

    @GetMapping("/")
    public String home() {
        return "JPop Ranker API is running!";
    }

    @GetMapping("/test")
    public String test() {
        return "Test endpoint works!";
    }

    @GetMapping("/health")
    public String health() {
        return "Server is healthy!";
    }
}
