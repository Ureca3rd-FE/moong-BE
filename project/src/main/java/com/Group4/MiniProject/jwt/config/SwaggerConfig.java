package com.Group4.MiniProject.jwt.config;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI openAPI() {
        // SecurityScheme 이름 정의
        String jwtSchemeName = "JWT Authorization";

        // SecurityRequirement 추가 (전역 적용)
        SecurityRequirement securityRequirement = new SecurityRequirement()
                .addList(jwtSchemeName);

        // SecurityScheme 설정
        Components components = new Components()
                .addSecuritySchemes(jwtSchemeName, new SecurityScheme()
                        .name(jwtSchemeName)
                        .type(SecurityScheme.Type.HTTP)  // HTTP 인증
                        .scheme("bearer")                 // Bearer 토큰
                        .bearerFormat("JWT"));            // JWT 포맷

        return new OpenAPI()
                .info(new Info()
                        .title("Mini Project API")
                        .description("눈사람 만들기 프로젝트 API 문서")
                        .version("1.0.0"))
                .addSecurityItem(securityRequirement)  // 전역 Security 적용
                .components(components);
    }
}