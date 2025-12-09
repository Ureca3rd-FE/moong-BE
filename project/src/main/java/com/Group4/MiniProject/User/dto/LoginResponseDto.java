package com.Group4.MiniProject.User.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class LoginResponseDto {

    private String accessToken;
    private long accessTokenExpiredAt;
    private String refreshToken;
    private long refreshTokenExpiredAt;

    /**
     * LoginResponseDto 생성 팩토리 메서드
     */
    public static LoginResponseDto of(
            String accessToken,
            long accessTokenExpiredAt,
            String refreshToken,
            long refreshTokenExpiredAt
    ) {
        return new LoginResponseDto(
                accessToken,
                accessTokenExpiredAt,
                refreshToken,
                refreshTokenExpiredAt
        );
    }
}