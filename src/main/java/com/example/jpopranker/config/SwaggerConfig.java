package com.example.jpopranker.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import java.util.List;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI jpopRankerOpenAPI() {
        return new OpenAPI()
            .info(new Info()
                .title("JPop Ranker API")
                .description("일본 음악 차트 크롤링 및 랭킹 서비스 API")
                .version("v1.0.0")
                .contact(new Contact()
                    .name("JPop Ranker Team")
                    .email("jpopranker@example.com"))
                .license(new License()
                    .name("MIT License")
                    .url("https://opensource.org/licenses/MIT")))
            .servers(List.of(
                new Server()
                    .url("http://localhost:8080")
                    .description("개발 서버")
            ));
    }
}
