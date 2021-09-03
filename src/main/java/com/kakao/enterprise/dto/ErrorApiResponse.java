package com.kakao.enterprise.dto;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class ErrorApiResponse {

    private final int code;

    private final String msg;
}
