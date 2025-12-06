package com.Group4.MiniProject.jwt.filter;

import com.Group4.MiniProject.jwt.components.JwtParser;
import com.Group4.MiniProject.jwt.components.JwtValidator;
import jakarta.servlet.http.HttpServletRequest;
import lombok.NonNull;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.servlet.HandlerExceptionResolver;

public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private static final String AUTHORIZATION_HEADER = "Authorization";
    private final HandlerExceptionResolver exceptionResolver;

    private final JwtValidator jwtValidator;
    private final JwtParser jwtParser;

    public JwtAuthenticationFilter(
            @Qualifier("handlerExceptionResolver") HandlerExceptionResolver,
            JwtValidator jwtValidator,
            JwtParser jwtParser
    ){
        this.exceptionResolver = exceptionResolver;
        this.jwtValidator = jwtValidator;
        this.jwtParser = jwtParser;
    }
}
