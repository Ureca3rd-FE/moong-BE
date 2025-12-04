package com.Group4.MiniProject.common.exception.user;

import com.Group4.MiniProject.common.exception.common.BaseException;
import com.Group4.MiniProject.common.exception.common.ErrorCode;

public class UserDuplicatedException extends BaseException {
    public UserDuplicatedException() {
        super(ErrorCode.NICKNAME_DUPLICATED);
    }
}
