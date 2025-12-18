package com.Group4.MiniProject.User.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Schema(description = "로그인 응답 DTO")
public class LoginResponseDto {

    private String accessToken;
    private long accessTokenExpiredAt;
    private String refreshToken;
    private long refreshTokenExpiredAt;
    private long userId;

    /**
     * LoginResponseDto 생성 팩토리 메서드
     */
    public static LoginResponseDto of(
            String accessToken,
            long accessTokenExpiredAt,
            String refreshToken,
            long refreshTokenExpiredAt,
            long userId
    ) {
        return new LoginResponseDto(
                accessToken,
                accessTokenExpiredAt,
                refreshToken,
                refreshTokenExpiredAt,
                userId
        );
    }
}