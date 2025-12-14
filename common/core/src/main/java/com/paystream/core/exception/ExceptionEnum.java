package com.paystream.core.exception;

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

    NOT_STORE_HOST("I0001", HttpStatus.FORBIDDEN, "해당 가게의 HostId와 다릅니다."),
    STORE_NOT_FOUND("I0002", HttpStatus.NOT_FOUND, "존재하지 않는 가게입니다."),
    STORE_ALREADY_EXISTS("I0003", HttpStatus.CONFLICT, "이미 존재하는 가게 이름입니다."),
    STORE_DELETION_BLOCKED("I0004", HttpStatus.CONFLICT, "삭제가 불가능한 가게가 있습니다 다시 확인해주세요."),
    STORE_ACCESS_DENIED("I0005", HttpStatus.FORBIDDEN, "가게 주인이 맞는지 다시 확인해주세요."),
    PRODUCT_NOT_FOUND("I0006", HttpStatus.NOT_FOUND, "존재하지 않는 상품입니다."),
    PRODUCT_ALREADY_EXISTS("I0007", HttpStatus.CONFLICT, "이미 존재하는 상품입니다."),
    PRODUCT_DELETION_BLOCKED("I0008", HttpStatus.CONFLICT, "삭제가 불가능한 상품이 있습니다 다시 확인해주세요."),

    // user-service -> U0001
    USER_NOT_FOUND("U0001", HttpStatus.NOT_FOUND, "존재하지 않는 사용자입니다."),
    USER_ALREADY_EXISTS("U0002", HttpStatus.CONFLICT, "이미 존재하는 이메일입니다."),
    INVALID_CREDENTIALS("U0003", HttpStatus.UNAUTHORIZED, "이메일 또는 비밀번호가 올바르지 않습니다."),
    INVALID_REFRESH_TOKEN("U0004", HttpStatus.UNAUTHORIZED, "유효하지 않은 리프레시 토큰입니다."),
    EXPIRED_REFRESH_TOKEN("U0005", HttpStatus.UNAUTHORIZED, "만료된 리프레시 토큰입니다."),
    EMAIL_NOT_VERIFIED("U0006", HttpStatus.FORBIDDEN, "이메일 인증이 완료되지 않았습니다."),
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
