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
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.servlet.HandlerExceptionResolver;
import org.springframework.security.core.context.SecurityContextHolder;

import java.io.IOException;
import java.util.Collections;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {  // 🔥 @RequiredArgsConstructor 제거!

    private static final String ACCESS_TOKEN_HEADER_KEY = "Authorization";
    private static final String NEW_ACCESS_TOKEN_HEADER = "X-New-Access-Token";

    // 🔥 필드 주입으로 변경!
    @Qualifier("handlerExceptionResolver")
    private final HandlerExceptionResolver exceptionResolver;

    private final JwtValidator jwtValidator;
    private final JwtParser jwtParser;
    private final JwtProvider jwtProvider;
    private final RefreshTokenExtractor refreshTokenExtractor;
    private final UserRepository userRepository;

    // 🔥 생성자 직접 작성
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
        boolean skip = WhiteList.isPermitted(request.getRequestURI(), request.getMethod());
        System.out.println("shouldNotFilter: " + request.getRequestURI() + " → " + skip);
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

        // 1. Access Token 형식 검증
        if (!jwtValidator.isValidFormat(accessTokenWithBearer)) {
            System.out.println("Access Token 형식 오류 → Refresh Token 확인");

            // Refresh Token으로 자동 갱신 시도
            if (tryRefreshToken(request, response, filterChain)) {
                return;  // 갱신 성공 → 필터 체인 계속
            }

            // Refresh Token도 없거나 만료됨
            resolveBaseException(request, response, new TokenInvalidException());
            return;
        }

        final String accessToken = jwtParser.extractToken(accessTokenWithBearer);

        // 2. Access Token 검증
        try {
            jwtValidator.verifyToken(accessToken);

            // 3. 인증 설정
            setAuthentication(accessToken);

            System.out.println("Access Token 인증 성공");

        } catch (TokenExpiredException e) {
            System.out.println("Access Token 만료 → Refresh Token으로 갱신 시도");

            // Access Token 만료 → Refresh Token으로 자동 갱신 시도
            if (tryRefreshToken(request, response, filterChain)) {
                return;  // 갱신 성공 → 필터 체인 계속
            }

            // Refresh Token도 만료됨
            resolveBaseException(request, response, e);
            return;

        } catch (BaseException e) {
            System.err.println("토큰 검증 실패: " + e.getMessage());
            resolveBaseException(request, response, e);
            return;
        }

        filterChain.doFilter(request, response);
    }

    /**
     * 🔥 핵심: Refresh Token으로 자동 갱신 시도
     */
    private boolean tryRefreshToken(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        try {
            System.out.println("Refresh Token 갱신 프로세스 시작");

            // 1. Refresh Token 추출 (쿠키 또는 헤더에서)
            String refreshToken = refreshTokenExtractor.extract(request);

            if (refreshToken == null) {
                System.out.println("Refresh Token이 없습니다.");
                return false;
            }

            // 2. Refresh Token 검증
            jwtValidator.verifyToken(refreshToken);

            // 3. Refresh Token 타입 확인
            if (!jwtValidator.isRefreshToken(refreshToken)) {
                System.err.println("Refresh Token이 아닙니다.");
                return false;
            }

            // 4. userId 추출
            Long userId = jwtParser.getUserId(refreshToken);
            System.out.println("Refresh Token에서 userId 추출: " + userId);

            // 5. User 조회
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new TokenInvalidException());

            // 6. 새 Access Token 발급
            TokenInfo newAccessTokenInfo = jwtProvider.generateAccessToken(user);
            String newAccessToken = newAccessTokenInfo.token();

            System.out.println("새 Access Token 발급 완료 - userId: " + userId);

            // 7. 인증 설정
            setAuthentication(newAccessToken);

            // 8. 프론트엔드에 새 토큰 전달
            response.setHeader(NEW_ACCESS_TOKEN_HEADER, newAccessToken);
            System.out.println("응답 헤더에 새 Access Token 추가: " + NEW_ACCESS_TOKEN_HEADER);

            // 9. 필터 체인 계속 진행
            filterChain.doFilter(request, response);

            return true;  // 갱신 성공

        } catch (TokenExpiredException e) {
            System.err.println("Refresh Token도 만료되었습니다.");
            return false;  // 갱신 실패

        } catch (BaseException e) {
            System.err.println("Refresh Token 검증 실패: " + e.getMessage());
            return false;  // 갱신 실패

        } catch (Exception e) {
            System.err.println("예상치 못한 오류 발생: " + e.getMessage());
            e.printStackTrace();
            return false;  // 갱신 실패
        }
    }

    /**
     * 인증 객체 설정
     */
    private void setAuthentication(String accessToken) {
        Long userId = jwtParser.getUserId(accessToken);

        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(
                        userId,
                        null,
                        Collections.emptyList()
                );

        SecurityContextHolder.getContext().setAuthentication(authentication);
        System.out.println("SecurityContext에 인증 정보 저장 완료 - userId: " + userId);
    }

    /**
     * 예외 처리
     */
    private void resolveBaseException(
            HttpServletRequest request,
            HttpServletResponse response,
            BaseException baseException
    ) {
        SecurityContextHolder.clearContext();
        System.err.println("JWT 인증 실패 - 예외 발생: " + baseException.getMessage());
        exceptionResolver.resolveException(request, response, null, baseException);
    }
}