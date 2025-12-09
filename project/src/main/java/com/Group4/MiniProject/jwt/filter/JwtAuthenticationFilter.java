package com.Group4.MiniProject.jwt.filter;

import com.Group4.MiniProject.common.exception.auth.TokenInvalidException;
import com.Group4.MiniProject.common.exception.common.BaseException;
import com.Group4.MiniProject.jwt.components.JwtParser;
import com.Group4.MiniProject.jwt.components.JwtValidator;
import com.Group4.MiniProject.jwt.util.WhiteList;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.servlet.HandlerExceptionResolver;
import org.springframework.security.core.context.SecurityContextHolder;


import java.io.IOException;
import java.util.Collections;

public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private static final String ACCESS_TOKEN_HEADER_KEY = "Authorization";
    private final HandlerExceptionResolver exceptionResolver;

    private final JwtValidator jwtValidator;
    private final JwtParser jwtParser;

    public JwtAuthenticationFilter(
            @Qualifier("handlerExceptionResolver") HandlerExceptionResolver exceptionResolver,
            JwtValidator jwtValidator,
            JwtParser jwtParser
    ){
        this.exceptionResolver = exceptionResolver;
        this.jwtValidator = jwtValidator;
        this.jwtParser = jwtParser;
    }

    @Override
    protected boolean shouldNotFilter(@NonNull HttpServletRequest request) {
        return WhiteList.isPermitted(request.getRequestURI(), request.getMethod());
    }

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {
        final String accessTokenWithBearer = request.getHeader(ACCESS_TOKEN_HEADER_KEY);

        if(!jwtValidator.isValidFormat(accessTokenWithBearer)){
            resolveBaseException(request,response, new TokenInvalidException());
            return;
        }
        final String accessToken = jwtParser.extractToken(accessTokenWithBearer);

        try{
            jwtValidator.verifyToken(accessToken);
        }catch (BaseException e){
            resolveBaseException(request,response,e);
            return;
        }
        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(Long.parseLong(jwtParser.getSubject(accessToken)), null,
                        Collections.emptyList());

        SecurityContextHolder.getContext().setAuthentication(authentication);

        filterChain.doFilter(request, response);
    }
    private void resolveBaseException(HttpServletRequest request, HttpServletResponse response,
                                      BaseException baseException) {
        SecurityContextHolder.clearContext();
        exceptionResolver.resolveException(request,response, null, baseException);
    }
}
