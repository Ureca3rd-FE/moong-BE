package com.Group4.MiniProject.common.exception.auth;

import com.Group4.MiniProject.common.exception.common.BaseException;
import com.Group4.MiniProject.common.exception.common.ErrorCode;
import jakarta.security.auth.message.AuthException;

public class TokenInvalidException extends BaseException {
    public TokenInvalidException() {
        super(ErrorCode.TOKEN_INVALID);
    }
}
