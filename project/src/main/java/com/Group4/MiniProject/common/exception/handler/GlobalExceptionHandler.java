package com.Group4.MiniProject.common.exception.handler;

import com.Group4.MiniProject.common.dto.ErrorResponseDto;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponseDto> handleIllegalArgumentException(IllegalArgumentException ex) {
        HttpStatus status = HttpStatus.BAD_REQUEST;

        ErrorResponseDto errorResponse = new ErrorResponseDto(ex.getMessage());

        // 400 상태 코드와 ErrorResponseDto를 반환
        return new ResponseEntity<>(errorResponse, status);
    }
}