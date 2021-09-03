package com.kakao.enterprise.dto;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class ErrorResponse {

    private String error;

    private String errorDescription;

    public ErrorResponse(ErrorApiResponse errorApiResponse) {

        this.error = String.valueOf(errorApiResponse.getCode());
        this.errorDescription = errorApiResponse.getMsg();
    }
}

