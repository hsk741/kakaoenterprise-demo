package com.kakao.enterprise.dto;

import lombok.Getter;

@Getter
public enum ResponseCode {

    NOT_EXISTED_TOKEN(400, "Token not existed!!!"),
    INVALID_TOKEN(401, "Invalid token!!!"),
    SYSTEM_ERROR(500, "Internal Server Error");

    private final int code;

    private final String message;

    ResponseCode(int code, String message) {

        this.code = code;
        this.message = message;
    }
}
