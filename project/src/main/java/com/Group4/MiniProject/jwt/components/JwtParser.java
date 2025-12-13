package com.Group4.MiniProject.jwt.components;

import com.Group4.MiniProject.common.exception.auth.TokenInvalidException;
import com.Group4.MiniProject.jwt.util.JwtProperties;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;

@Component
@RequiredArgsConstructor
public class JwtParser {
    private final JwtProvider jwtProvider;
    private final JwtProperties jwtProperties;

    public String extractToken(final String tokenWithBearer){
        final String prefix = jwtProperties.prefix() + " ";
        return tokenWithBearer.substring(prefix.length());
    }
    public String getSubject(final String token){
        return parseClaims(token).getPayload()
                .getSubject();
    }
    public long getExpire(final String token){
        return parseClaims(token).getPayload()
                .getExpiration().getTime();
    }

    public Long getUserId(final String token) {
        try {
            Claims claims = parseClaims(token).getPayload();
            Object userIdObj = claims.get("userId");

            if (userIdObj == null) {
                // userId claim이 없으면 subject를 userId로 사용
                return Long.parseLong(claims.getSubject());
            }

            // Integer나 Long으로 저장되어 있을 수 있음
            if (userIdObj instanceof Integer) {
                return ((Integer) userIdObj).longValue();
            } else if (userIdObj instanceof Long) {
                return (Long) userIdObj;
            } else {
                return Long.parseLong(userIdObj.toString());
            }
        } catch (Exception e) {
            System.err.println("토큰에서 UserId 추출 실패: " + e.getMessage());
            throw new TokenInvalidException();
        }
    }

    /**
     * 토큰에서 nickname 추출
     */
    public String getNickname(final String token) {
        try {
            Claims claims = parseClaims(token).getPayload();
            return claims.get("nickname", String.class);
        } catch (JwtException e) {
            System.err.println("토큰에서 Nickname 추출 실패: " + e.getMessage());
            throw new TokenInvalidException();
        }
    }

    public String getTokenType(final String token) {
        try {
            Claims claims = parseClaims(token).getPayload();
            return claims.get("tokenType", String.class);
        } catch (JwtException e) {
            System.err.println("토큰에서 TokenType 추출 실패: " + e.getMessage());
            throw new TokenInvalidException();
        }
    }



    public Jws<Claims> parseClaims(final String token){
        SecretKey secretKey = jwtProvider.getSecretKey();
        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token);
    }
}
