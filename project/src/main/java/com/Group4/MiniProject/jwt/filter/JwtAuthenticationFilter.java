package com.Group4.MiniProject.jwt.filter;

import com.Group4.MiniProject.User.entity.User;
import com.Group4.MiniProject.User.repository.UserRepository;
import com.Group4.MiniProject.common.exception.auth.TokenInvalidException;
import com.Group4.MiniProject.common.exception.auth.TokenExpiredException;
import com.Group4.MiniProject.common.exception.common.BaseException;
import com.Group4.MiniProject.jwt.components.*;
import com.Group4.MiniProject.jwt.util.TokenInfo;
import com.Group4.MiniProject.jwt.util.WhiteList;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.servlet.HandlerExceptionResolver;
import org.springframework.security.core.context.SecurityContextHolder;

import java.io.IOException;
import java.util.Collections;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final String ACCESS_TOKEN_HEADER_KEY = "Authorization";
    private static final String NEW_ACCESS_TOKEN_HEADER = "X-New-Access-Token";

    private final HandlerExceptionResolver exceptionResolver;
    private final JwtValidator jwtValidator;
    private final JwtParser jwtParser;
    private final JwtProvider jwtProvider;
    private final RefreshTokenExtractor refreshTokenExtractor;
    private final UserRepository userRepository;

    public JwtAuthenticationFilter(
            @Qualifier("handlerExceptionResolver") HandlerExceptionResolver exceptionResolver,
            JwtValidator jwtValidator,
            JwtParser jwtParser,
            JwtProvider jwtProvider,
            RefreshTokenExtractor refreshTokenExtractor,
            UserRepository userRepository
    ) {
        this.exceptionResolver = exceptionResolver;
        this.jwtValidator = jwtValidator;
        this.jwtParser = jwtParser;
        this.jwtProvider = jwtProvider;
        this.refreshTokenExtractor = refreshTokenExtractor;
        this.userRepository = userRepository;
    }

    @Override
    protected boolean shouldNotFilter(@NonNull HttpServletRequest request) {
        String path = request.getRequestURI();

        // ✅ [수정] WhiteList에 등록되지 않았더라도 /api/user/ 경로는 필터를 건너뛰도록 강제 설정
        boolean isPublicPath = path.startsWith("/api/user/") ||
                path.startsWith("/api/member/homeinfo/");

        boolean skip = isPublicPath || WhiteList.isPermitted(path, request.getMethod());

        System.out.println("shouldNotFilter 체크: " + path + " → " + skip);
        return skip;
    }

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {

        System.out.println("=== JWT 인증 필터 시작: " + request.getMethod() + " " + request.getRequestURI() + " ===");

        final String accessTokenWithBearer = request.getHeader(ACCESS_TOKEN_HEADER_KEY);

        // 토큰이 없거나 형식이 틀린 경우, 다음 필터(SecurityConfig의 permitAll 체크)로 넘김
        if (accessTokenWithBearer == null || !jwtValidator.isValidFormat(accessTokenWithBearer)) {
            System.out.println("Access Token 없음 또는 형식 오류 → 다음 필터로 진행");
            filterChain.doFilter(request, response);
            return;
        }

        final String accessToken = jwtParser.extractToken(accessTokenWithBearer);

        // Access Token 검증
        try {
            jwtValidator.verifyToken(accessToken);
            setAuthentication(accessToken);
            System.out.println("Access Token 인증 성공");
        } catch (TokenExpiredException e) {
            System.out.println("Access Token 만료 → Refresh Token으로 갱신 시도");
            if (tryRefreshToken(request, response, filterChain)) {
                return;
            }
            resolveBaseException(request, response, e);
            return;
        } catch (BaseException e) {
            System.err.println("토큰 검증 실패: " + e.getMessage());
            resolveBaseException(request, response, e);
            return;
        }

        filterChain.doFilter(request, response);
    }

    private boolean tryRefreshToken(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        try {
            System.out.println("Refresh Token 갱신 프로세스 시작");
            String refreshToken = refreshTokenExtractor.extract(request);

            if (refreshToken == null) {
                System.out.println("Refresh Token이 없습니다.");
                return false;
            }

            jwtValidator.verifyToken(refreshToken);

            if (!jwtValidator.isRefreshToken(refreshToken)) {
                System.err.println("Refresh Token이 아닙니다.");
                return false;
            }

            Long userId = jwtParser.getUserId(refreshToken);
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new TokenInvalidException());

            TokenInfo newAccessTokenInfo = jwtProvider.generateAccessToken(user);
            String newAccessToken = newAccessTokenInfo.token();

            setAuthentication(newAccessToken);
            response.setHeader(NEW_ACCESS_TOKEN_HEADER, newAccessToken);

            filterChain.doFilter(request, response);
            return true;

        } catch (Exception e) {
            System.err.println("Refresh Token 처리 중 오류: " + e.getMessage());
            return false;
        }
    }

    private void setAuthentication(String accessToken) {
        Long userId = jwtParser.getUserId(accessToken);
        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(userId, null, Collections.emptyList());
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    private void resolveBaseException(HttpServletRequest request, HttpServletResponse response, BaseException baseException) {
        SecurityContextHolder.clearContext();
        exceptionResolver.resolveException(request, response, null, baseException);
    }
}