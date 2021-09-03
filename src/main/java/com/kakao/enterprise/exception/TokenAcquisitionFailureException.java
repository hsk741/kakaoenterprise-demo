package com.kakao.enterprise.exception;

import com.kakao.enterprise.dto.ResponseCode;

public class TokenAcquisitionFailureException extends BusinessException {

    public TokenAcquisitionFailureException() {
        super(ResponseCode.NOT_EXISTED_TOKEN);
    }

    public TokenAcquisitionFailureException(String message) {
        super(ResponseCode.NOT_EXISTED_TOKEN.getCode(), message);
    }

    public TokenAcquisitionFailureException(Throwable cause) {
        super(ResponseCode.NOT_EXISTED_TOKEN, cause);
    }
}
