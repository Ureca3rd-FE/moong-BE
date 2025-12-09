package com.Group4.MiniProject.jwt.components;

import com.Group4.MiniProject.User.entity.User;
import com.Group4.MiniProject.jwt.util.TokenInfo;
import com.Group4.MiniProject.jwt.util.JwtProperties;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtProvider {

    private final JwtProperties jwtProperties;
    private SecretKey secretKey;  // 필드로 저장

    /**
     * Access Token 생성
     */
    public TokenInfo generateAccessToken(final User user) {
        log.debug("Access Token 생성 - userId: {}, nickname: {}", user.getId(), user.getNickname());
        return generateToken(user, jwtProperties.accessTokenExpireIn(), "ACCESS");
    }

    /**
     * Refresh Token 생성
     */
    public TokenInfo generateRefreshToken(final User user) {
        log.debug("Refresh Token 생성 - userId: {}, nickname: {}", user.getId(), user.getNickname());
        return generateToken(user, jwtProperties.refreshTokenExpireIn(), "REFRESH");
    }

    /**
     * Token 생성 (공통 메서드)
     */
    private TokenInfo generateToken(final User user, final long expiration, final String tokenType) {
        long now = System.currentTimeMillis();
        long expiredAt = now + expiration;

        String token = Jwts.builder()
                .issuer(jwtProperties.issuer())
                .subject(user.getNickname())
                .claim("userId", user.getId())
                .claim("nickname", user.getNickname())
                .claim("tokenType", tokenType)
                .issuedAt(new Date(now))
                .expiration(new Date(expiredAt))
                .signWith(secretKey, SignatureAlgorithm.HS512)  // 필드 직접 사용
                .compact();

        log.debug("Token 생성 완료 - userId: {}, tokenType: {}, expiredAt: {}",
                user.getId(), tokenType, new Date(expiredAt));

        return TokenInfo.of(token, expiredAt);
    }
}