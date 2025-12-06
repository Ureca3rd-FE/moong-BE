package com.Group4.MiniProject.jwt.components;

import com.Group4.MiniProject.jwt.util.JwtProperties;
import com.Group4.MiniProject.common.exception.auth.TokenExpiredException;
import com.Group4.MiniProject.common.exception.auth.TokenInvalidException;
import com.Group4.MiniProject.common.exception.auth.UnAuthorizedException;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtValidator {
    private final JwtProperties jwtProperties;
    private SecretKey secretKey;  // 필드로 저장

    /**
     * 초기화: 애플리케이션 시작 시 한 번만 SecretKey 생성
     */
    @PostConstruct
    public void init() {
        byte[] keyBytes = jwtProperties.getSalt().getBytes(StandardCharsets.UTF_8);
        this.secretKey = Keys.hmacShaKeyFor(keyBytes);
        log.info("JwtValidator 초기화 완료 - SecretKey 생성됨");
    }

    /**
     * 토큰 검증
     */
    public boolean validateToken(String token) {
        try {
            Jwts.parser()
                    .verifyWith(secretKey)  // 필드 직접 사용
                    .build()
                    .parseSignedClaims(token);

            log.debug("토큰 검증 성공");
            return true;

        } catch (ExpiredJwtException e) {
            log.error("토큰 만료: {}", e.getMessage());
            throw new TokenExpiredException();
        } catch (UnsupportedJwtException e) {
            log.error("지원하지 않는 토큰: {}", e.getMessage());
            throw new TokenInvalidException();
        } catch (MalformedJwtException e) {
            log.error("잘못된 형식의 토큰: {}", e.getMessage());
            throw new TokenInvalidException();
        } catch (SignatureException e) {
            log.error("서명 검증 실패: {}", e.getMessage());
            throw new TokenInvalidException();
        } catch (IllegalArgumentException e) {
            log.error("토큰이 null이거나 비어있음: {}", e.getMessage());
            throw new TokenInvalidException();
        } catch (Exception e) {
            log.error("토큰 검증 중 오류: {}", e.getMessage());
            throw new TokenInvalidException();
        }
    }

    /**
     * Claims 추출 (공통 메서드)
     */
    private Claims getClaims(String token) {
        try {
            return Jwts.parser()
                    .verifyWith(secretKey)  // 필드 직접 사용
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (Exception e) {
            log.error("Claims 추출 실패: {}", e.getMessage());
            throw new UnAuthorizedException("유효하지 않은 토큰입니다.");
        }
    }

    /**
     * 토큰에서 사용자 ID 추출
     */
    public Long getUserId(String token) {
        try {
            Claims claims = getClaims(token);
            Long userId = claims.get("userId", Long.class);
            log.debug("userId 추출: {}", userId);
            return userId;
        } catch (Exception e) {
            log.error("userId 추출 실패: {}", e.getMessage());
            throw new UnAuthorizedException("토큰에서 사용자 정보를 추출할 수 없습니다.");
        }
    }

    /**
     * 토큰에서 닉네임 추출
     */
    public String getNickname(String token) {
        try {
            Claims claims = getClaims(token);
            String nickname = claims.get("nickname", String.class);
            log.debug("nickname 추출: {}", nickname);
            return nickname;
        } catch (Exception e) {
            log.error("nickname 추출 실패: {}", e.getMessage());
            throw new UnAuthorizedException("토큰에서 닉네임을 추출할 수 없습니다.");
        }
    }

    /**
     * 토큰 타입 추출 (ACCESS/REFRESH)
     */
    public String getTokenType(String token) {
        try {
            Claims claims = getClaims(token);
            String tokenType = claims.get("tokenType", String.class);
            log.debug("tokenType 추출: {}", tokenType);
            return tokenType;
        } catch (Exception e) {
            log.error("tokenType 추출 실패: {}", e.getMessage());
            throw new UnAuthorizedException("토큰 타입을 확인할 수 없습니다.");
        }
    }

    /**
     * 토큰의 남은 유효 시간 (밀리초)
     */
    public long getTimeToExpire(String token) {
        try {
            Claims claims = getClaims(token);
            Date expiration = claims.getExpiration();
            long now = System.currentTimeMillis();
            long timeToExpire = expiration.getTime() - now;

            log.debug("토큰 남은 시간: {}ms", timeToExpire);
            return Math.max(0, timeToExpire);
        } catch (Exception e) {
            log.error("만료 시간 확인 실패: {}", e.getMessage());
            return 0;
        }
    }

    /**
     * 토큰이 만료되었는지 확인
     */
    public boolean isTokenExpired(String token) {
        try {
            return getTimeToExpire(token) <= 0;
        } catch (Exception e) {
            return true;
        }
    }
}
