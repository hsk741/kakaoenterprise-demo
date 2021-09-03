package com.kakao.enterprise.exception;

import com.kakao.enterprise.dto.ResponseCode;

public class InvalidBearerTokenException extends BusinessException {

    public InvalidBearerTokenException() {
        super(ResponseCode.INVALID_TOKEN);
    }

    public InvalidBearerTokenException(String message) {
        super(ResponseCode.INVALID_TOKEN.getCode(), message);
    }

    public InvalidBearerTokenException(Throwable cause) {
        super(ResponseCode.INVALID_TOKEN, cause);
    }
}