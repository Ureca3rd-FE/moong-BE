package com.Group4.MiniProject.jwt.components;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class RefreshTokenExtractor {

    private static final String REFRESH_TOKEN_COOKIE_NAME = "refreshToken";
    private static final String REFRESH_TOKEN_HEADER = "X-Refresh-Token";

    /**
     * 쿠키 또는 헤더에서 Refresh Token 추출
     */
    public String extract(HttpServletRequest request) {
        // 1. 쿠키에서 먼저 찾기 (우선순위 높음)
        String tokenFromCookie = extractFromCookie(request);
        if (tokenFromCookie != null) {
            log.debug("쿠키에서 Refresh Token 추출 완료");
            return tokenFromCookie;
        }

        // 2. 헤더에서 찾기 (백업)
        String tokenFromHeader = extractFromHeader(request);
        if (tokenFromHeader != null) {
            log.debug("헤더에서 Refresh Token 추출 완료");
            return tokenFromHeader;
        }

        log.debug("Refresh Token을 찾을 수 없습니다.");
        return null;
    }

    /**
     * 쿠키에서 Refresh Token 추출
     */
    private String extractFromCookie(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();

        if (cookies == null) {
            return null;
        }

        for (Cookie cookie : cookies) {
            if (REFRESH_TOKEN_COOKIE_NAME.equals(cookie.getName())) {
                return cookie.getValue();
            }
        }

        return null;
    }

    /**
     * 헤더에서 Refresh Token 추출
     */
    private String extractFromHeader(HttpServletRequest request) {
        String header = request.getHeader(REFRESH_TOKEN_HEADER);

        if (header != null && header.startsWith("Bearer ")) {
            return header.substring(7);
        }

        return header;
    }
}