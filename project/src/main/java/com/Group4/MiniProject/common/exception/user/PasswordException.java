package com.Group4.MiniProject.common.exception.user;

import com.Group4.MiniProject.common.exception.common.BaseException;
import com.Group4.MiniProject.common.exception.common.ErrorCode;

public class PasswordException extends BaseException {
    public PasswordException(){
        super(ErrorCode.PASSWORD_MISMATCH);
    }
}
