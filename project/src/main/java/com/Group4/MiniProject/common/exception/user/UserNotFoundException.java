package com.Group4.MiniProject.common.exception.user;

import com.Group4.MiniProject.common.exception.common.BaseException;
import com.Group4.MiniProject.common.exception.common.ErrorCode;

public class UserNotFoundException extends BaseException {
    public UserNotFoundException(){
        super(ErrorCode.USER_NOT_FOUND);
    }
}
