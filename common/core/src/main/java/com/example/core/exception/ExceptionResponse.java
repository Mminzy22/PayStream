package com.example.core.exception;

import lombok.Builder;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class ExceptionResponse {

    private String code;
    private HttpStatus status;
    private String message;

    @Builder
    private ExceptionResponse(String code, HttpStatus status, String message) {
        this.code = code;
        this.status = status;
        this.message = message;
    }

}
