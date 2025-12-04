package com.Group4.MiniProject.common.exception.content;

import com.Group4.MiniProject.common.exception.common.BaseException;
import com.Group4.MiniProject.common.exception.common.ErrorCode;

public class ContentNotFoundException extends BaseException {
    public ContentNotFoundException() {
        super(ErrorCode.CONTENT_NOT_FOUND);
    }
}
