package com.Group4.MiniProject.common.exception.image;

import com.Group4.MiniProject.common.exception.common.BaseException;
import com.Group4.MiniProject.common.exception.common.ErrorCode;

public class ImageFormatException extends BaseException {
    public ImageFormatException() {
        super(ErrorCode.INVALID_IMAGE_FORMAT);
    }
}
