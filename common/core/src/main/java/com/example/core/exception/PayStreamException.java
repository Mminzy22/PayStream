package com.example.core.exception;

import lombok.Getter;

@Getter
public class PayStreamException extends RuntimeException {

    private final ExceptionEnum error;

    public PayStreamException(ExceptionEnum e) {
        super(e.getMessage());
        this.error = e;
    }

}
