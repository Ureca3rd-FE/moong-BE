package com.Group4.MiniProject.jwt.util;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;


public record TokenInfo(
            @Schema(description = "JWT 토큰 문자열")
            String token,

            @Schema(description = "만료 시각 (밀리초)")
            long expiredAt,

            @Schema(description = "만료까지 남은 시간 (서버계산)")
            long expiresIn
    ) {

        public static TokenInfo of(final String token, final long expiredAt) {
            long now = Instant.now().toEpochMilli();
            long expiresIn = Math.max(0, expiredAt - now);
            return new TokenInfo(token, expiredAt, expiresIn);
        }

        public long expiresInSeconds() {
            return expiresIn / 1000;
        }

        public boolean isExpired() {
            return expiresIn <= 0;
        }
    }