package com.Group4.MiniProject.common.exception.content;

import com.Group4.MiniProject.common.exception.common.BaseException;
import com.Group4.MiniProject.common.exception.common.ErrorCode;

public class ContentAccessDeniedException extends BaseException {
    public ContentAccessDeniedException() {
        super(ErrorCode.CONTENT_ACCESS_DENIED);
    }
}
