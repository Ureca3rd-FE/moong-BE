package com.Group4.MiniProject.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import lombok.Getter;
import lombok.Setter;

@Configuration
@ConfigurationProperties(prefix = "jwt")
@Getter
@Setter
public class JwtProperties {
    private String salt;
    private String issuer;
    private long accessTokenExpireIn;
    private long refreshTokenExpireIn;
}