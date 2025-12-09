package com.Group4.MiniProject.common.exception.common;

/**
 * 리소스를 찾을 수 없을 때 발생하는 예외
 */
public class NotFoundException extends RuntimeException {

    public NotFoundException() {
        super("리소스를 찾을 수 없습니다.");
    }

    public NotFoundException(String message) {
        super(message);
    }
}