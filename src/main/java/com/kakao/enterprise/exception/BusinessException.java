package com.kakao.enterprise.exception;

import com.kakao.enterprise.dto.ResponseCode;
import lombok.Getter;

@Getter
public class BusinessException extends RuntimeException {

    private int code;

    public BusinessException(int code, String message) {

        super(message);
        this.code = code;
    }

    public BusinessException(ResponseCode responseCode) {

        super(responseCode.getMessage());
        this.code = responseCode.getCode();
    }

    public BusinessException(int code, String message, Throwable cause) {

        super(message, cause);
        this.code = code;
    }

    public BusinessException(ResponseCode responseCode, Throwable cause) {

        super(responseCode.getMessage(), cause);
        this.code = responseCode.getCode();
    }
}