package com.example.core.exception;

import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class PayStreamExceptionAdvice {

    @ExceptionHandler({PayStreamException.class})
    public ExceptionResponse exceptionHandler(PayStreamException e) {
        return ExceptionResponse.builder()
                .code(e.getError().getCode())
                .status(e.getError().getStatus())
                .message(e.getMessage())
                .build();
    }

}
