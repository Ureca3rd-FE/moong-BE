package com.Group4.MiniProject.jwt.util;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpMethod;
import org.springframework.util.AntPathMatcher;
import java.util.Arrays;

@Getter
@RequiredArgsConstructor
public enum WhiteList {
    SWAGGER_V3_JSON("/v3/api-docs/**", HttpMethod.GET),
    SWAGGER_RESOURCES("/swagger-resources/**", HttpMethod.GET),
    SWAGGER_UI("/swagger-ui/**", HttpMethod.GET),
    SWAGGER_UI_LEGACY("/swagger-ui.html", HttpMethod.GET),
    SWAGGER_UI_INDEX("/swagger-ui/index.html", HttpMethod.GET),
    SWAGGER_STATIC_RESOURCES("/webjars/**", HttpMethod.GET),

    MEMBER_SIGNUP("/api/member/signup", HttpMethod.POST),
    MEMBER_LOGIN("/api/member/login", HttpMethod.POST),      // 🔥 수정!
    MEMBER_REFRESH("/api/member/refresh", HttpMethod.POST),  // 🔥 추가!
    MEMBER_TEST("/api/member/test", HttpMethod.GET);

    private static final AntPathMatcher pathMatcher = new AntPathMatcher();
    private final String path;
    private final HttpMethod method;

    public static boolean isPermitted(String requestPath, String httpMethod) {
        return Arrays.stream(values())
                .anyMatch(entry -> pathMatcher.match(entry.getPath(), requestPath)
                        && entry.getMethod().equals(HttpMethod.valueOf(httpMethod)));
    }

    public static String[] getAllowedPaths() {
        return Arrays.stream(values())
                .map(WhiteList::getPath)
                .toArray(String[]::new);
    }
}
