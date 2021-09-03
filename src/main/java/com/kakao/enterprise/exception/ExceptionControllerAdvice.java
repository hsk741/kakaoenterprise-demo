package com.kakao.enterprise.exception;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kakao.enterprise.dto.ErrorApiResponse;
import com.kakao.enterprise.dto.ErrorResponse;
import com.kakao.enterprise.dto.ResponseCode;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import javax.persistence.PersistenceException;

@RestControllerAdvice
@RequiredArgsConstructor
public class ExceptionControllerAdvice extends ResponseEntityExceptionHandler {

    private final ObjectMapper objectMapper;

    @ExceptionHandler
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorApiResponse businessException(BusinessException businessException) {

        businessException.printStackTrace();

        return new ErrorApiResponse(businessException.getCode(), businessException.getMessage());
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ErrorApiResponse persistenceException(PersistenceException persistenceException) {
        return new ErrorApiResponse(ResponseCode.SYSTEM_ERROR.getCode(), persistenceException.getMessage());
    }

    @ExceptionHandler
    public ResponseEntity<ErrorResponse> httpClientErrorException(HttpClientErrorException httpClientErrorException) {

        httpClientErrorException.printStackTrace();

        final ErrorResponse errorResponse;
        try {

            final String responseBodyAsString = httpClientErrorException.getResponseBodyAsString();

            errorResponse = responseBodyAsString.trim().contains("\"msg\":\"") ?
                    new ErrorResponse(objectMapper.readValue(responseBodyAsString, ErrorApiResponse.class)) :
                    objectMapper.readValue(responseBodyAsString, ErrorResponse.class);
        } catch (JsonProcessingException ex) {
            throw new RuntimeException(ex);
        }

        return ResponseEntity.status(httpClientErrorException.getStatusCode()).body(errorResponse);
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ErrorApiResponse runtimeException(RuntimeException runtimeException) {

        runtimeException.printStackTrace();

        return new ErrorApiResponse(ResponseCode.SYSTEM_ERROR.getCode(), runtimeException.getMessage());
    }
}
