package com.example.core.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ExceptionEnum {

    // System Exception
    RUNTIME_EXCEPTION("E0001", HttpStatus.BAD_REQUEST),
    ACCESS_DENIED_EXCEPTION("E0002", HttpStatus.UNAUTHORIZED),
    INTERNAL_SERVER_ERROR("E0003", HttpStatus.INTERNAL_SERVER_ERROR),

    /*
       inventory-service -> I0001
       notification-service -> N0001
       order-service -> O0001
       payment-service -> P0001
       user-service -> U0001
    */

    STORE_NOT_FOUND("I0001", HttpStatus.NOT_FOUND, "존재하지 않는 가게입니다."),
    ;

    private String code;
    private HttpStatus status;
    private String message;

    ExceptionEnum(String code, HttpStatus status) {
        this.code = code;
        this.status = status;
    }

    ExceptionEnum(String code, HttpStatus status, String message) {
        this.code = code;
        this.status = status;
        this.message = message;
    }
}
