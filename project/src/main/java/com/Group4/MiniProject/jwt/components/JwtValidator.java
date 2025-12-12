package com.Group4.MiniProject.jwt.components;

import com.Group4.MiniProject.jwt.util.JwtProperties;
import com.Group4.MiniProject.common.exception.auth.TokenExpiredException;
import com.Group4.MiniProject.common.exception.auth.TokenInvalidException;

import io.jsonwebtoken.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.Objects;

@Component
@RequiredArgsConstructor
public class JwtValidator {
    private final JwtProperties jwtProperties;
    private final JwtParser jwtParser;

    public boolean isValidFormat(final String tokenWithBearer) {
        final String prefix = jwtProperties.prefix() + " ";
        return tokenWithBearer != null
                && tokenWithBearer.startsWith(prefix)
                && tokenWithBearer.length() > prefix.length();
    }
    public void verifyToken(final String token) {
        Jws<Claims> claims = parseAndVerifySignature(token);
        verifyClaims(claims);
    }

    private Jws<Claims> parseAndVerifySignature(final String token) {
        try {
            return jwtParser.parseClaims(token);
        } catch (ExpiredJwtException e) {
            // 만료된 토큰
            throw new TokenExpiredException();
        } catch (UnsupportedJwtException | MalformedJwtException |
                 SignatureException | IllegalArgumentException e) {
            // 유효하지 않은 토큰
            throw new TokenInvalidException();
        } catch (JwtException e) {
            // 기타 JWT 예외
            throw new TokenInvalidException();
        }
    }

    public boolean isRefreshToken(final String token) {
        try {
            String tokenType = jwtParser.getTokenType(token);
            boolean isRefresh = "REFRESH".equals(tokenType);

            // 🔥 수정: String.format 사용
            System.err.println(String.format("토큰 타입 확인: %s (Refresh Token: %s)",
                    tokenType, isRefresh));

            return isRefresh;

        } catch (Exception e) {
            // 🔥 수정: log 제거
            System.err.println("토큰 타입 확인 실패: " + e.getMessage());
            return false;
        }
    }

    private void verifyClaims(final Jws<Claims> claims) {
        if (!isValidIssuer(claims)) {
            throw new TokenInvalidException();
        }

        if (isExpired(claims)) {
            throw new TokenExpiredException();
        }
    }
    private boolean isValidIssuer(final Jws<Claims> claims) {
        String issuer = claims.getPayload().getIssuer();
        return Objects.equals(issuer, jwtProperties.issuer());
    }

    private boolean isExpired(final Jws<Claims> claims) {
        Date expiration = claims.getPayload().getExpiration();

        if (expiration == null) {
            return true;
        }

        return expiration.before(new Date());
    }
}
