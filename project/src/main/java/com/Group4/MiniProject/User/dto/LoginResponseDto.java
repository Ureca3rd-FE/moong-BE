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

    @Schema(description = "첫 로그인 여부 (true: 첫 로그인, false: 기존 유저)")
    private boolean isFirst; // ✅ 추가됨

    /**
     * LoginResponseDto 생성 팩토리 메서드
     */
    public static LoginResponseDto of(
            String accessToken,
            long accessTokenExpiredAt,
            String refreshToken,
            long refreshTokenExpiredAt,
            long userId,
            boolean isFirst // ✅ 파라미터 추가
    ) {
        return new LoginResponseDto(
                accessToken,
                accessTokenExpiredAt,
                refreshToken,
                refreshTokenExpiredAt,
                userId,
                isFirst // ✅ 필드 초기화
        );
    }
}