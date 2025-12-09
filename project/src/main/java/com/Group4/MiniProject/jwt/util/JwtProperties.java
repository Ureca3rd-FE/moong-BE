package com.Group4.MiniProject.jwt.util;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "jwt")
public record JwtProperties (
        String secret,
        String issuer,
        long accessTokenExpireIn,
        long refreshTokenExpireIn,
        String prefix
){

}