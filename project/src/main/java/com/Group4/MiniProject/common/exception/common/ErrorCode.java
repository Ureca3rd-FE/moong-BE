package com.Group4.MiniProject.common.exception.common;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {
    // ========== 인증 관련 (401) ==========
    UNAUTHORIZED("인증이 필요합니다.", 401),
    TOKEN_EXPIRED("토큰이 만료되었습니다.", 401),
    TOKEN_INVALID("유효하지 않은 토큰입니다.", 401),

    // ========== 권한 관련 (403) ==========
    FORBIDDEN("권한이 없습니다.", 403),

    // ========== 사용자 관련 (400, 404) ==========
    USER_NOT_FOUND("사용자를 찾을 수 없습니다.", 404),
    NICKNAME_DUPLICATED("이미 존재하는 닉네임입니다.", 400),
    PASSWORD_MISMATCH("비밀번호가 일치하지 않습니다.", 400),

    // ========== 컨텐츠 관련 (404, 403) ==========
    CONTENT_NOT_FOUND("컨텐츠를 찾을 수 없습니다.", 404),
    CONTENT_ACCESS_DENIED("컨텐츠 접근 권한이 없습니다.", 403),

    // ========== 이미지 관련 (400, 500) ==========
    IMAGE_UPLOAD_FAILED("이미지 업로드에 실패했습니다.", 500),
    IMAGE_NOT_FOUND("이미지를 찾을 수 없습니다.", 404),
    INVALID_IMAGE_FORMAT("지원하지 않는 이미지 형식입니다.", 400),

    // ========== 서버 에러 (500) ==========
    INTERNAL_SERVER_ERROR("서버 에러가 발생했습니다.", 500);

    private final String message;
    private final int status;
}