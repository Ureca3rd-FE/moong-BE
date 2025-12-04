package com.Group4.MiniProject.common.exception.auth;

import com.Group4.MiniProject.common.exception.common.BaseException;
import com.Group4.MiniProject.common.exception.common.ErrorCode;

public class UnAuthorizedException extends BaseException {
    public UnAuthorizedException() {
        super(ErrorCode.UNAUTHORIZED);
    }
    public UnAuthorizedException(String message) {
        super(ErrorCode.UNAUTHORIZED, message);
    }
}
