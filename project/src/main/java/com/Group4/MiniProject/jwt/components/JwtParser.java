package com.Group4.MiniProject.jwt.components;

import com.Group4.MiniProject.jwt.util.JwtProperties;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;

@Component
@RequiredArgsConstructor
public class JwtParser {
    private final SecretKey secretKey;
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
    
    public Jws<Claims> parseClaims(final String token){
        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token);
    }
}
