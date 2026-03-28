package com.paystream.core.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ExceptionEnum {

    // System Exception
    RUNTIME_EXCEPTION("E0001", HttpStatus.BAD_REQUEST),
    ACCESS_DENIED_EXCEPTION("E0002", HttpStatus.UNAUTHORIZED, "인증되지 않은 사용자 입니다."),
    INTERNAL_SERVER_ERROR("E0003", HttpStatus.INTERNAL_SERVER_ERROR),
    INVALID_STATUS_VALUE("E0004", HttpStatus.BAD_REQUEST, "상태값을 다시 확인해주세요."),
    IS_NOT_CREATE_USER("E0005", HttpStatus.FORBIDDEN, "사용자의 정보가 다릅니다."),

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
    PRODUCT_STORE_MISMATCH("I0009", HttpStatus.BAD_REQUEST, "가게에 포함된 상품이 아닙니다."),
    INVENTORY_NOT_FOUND("I0010", HttpStatus.NOT_FOUND, "상품의 재고가 존재하지 않습니다."),
    INVALID_DATE_RANGE("I0011", HttpStatus.BAD_REQUEST, "체크인 날짜와 체크아웃 날짜가 바뀌었습니다. 다시 확인해주세요."),
    OUT_OF_BOOKING_PERIOD("I0012", HttpStatus.BAD_REQUEST, "현재 예약 가능 기간이 아닙니다."),
    INSUFFICIENT_STOCK("I0013", HttpStatus.CONFLICT, "재고가 부족한 날짜가 있습니다. 다시 확인해주세요."),
    INVALID_STOCK_CANCEL_REQUEST("I0014", HttpStatus.BAD_REQUEST, "재고 정보가 올바르지 않아 취소할 수 없습니다."),
    PROMOTION_NOT_FOUND("I0015", HttpStatus.NOT_FOUND, "프로모션을 찾을 수 없습니다."),
    PROMOTION_NOT_PERIOD("I0016", HttpStatus.BAD_REQUEST, "프로모션 날짜를 잘못 입력했습니다. 다시 확인해주세요."),
    PROMOTION_NOT_RATE("I0017", HttpStatus.BAD_REQUEST, "할인율을 잘못입력하셨습니다."),
    PROMOTION_DUPLICATE("I0018", HttpStatus.BAD_REQUEST, "중복된 프로모션이 있습니다."),
    PROMOTION_TYPE_DOES_NOT_EXIST("I0019", HttpStatus.BAD_REQUEST, "입력하신 프로모션 적용 타입이 존재하지 않습니다."),

    // user-service -> U0001
    USER_NOT_FOUND("U0001", HttpStatus.NOT_FOUND, "존재하지 않는 사용자입니다."),
    USER_ALREADY_EXISTS("U0002", HttpStatus.CONFLICT, "이미 존재하는 이메일입니다."),
    INVALID_CREDENTIALS("U0003", HttpStatus.UNAUTHORIZED, "이메일 또는 비밀번호가 올바르지 않습니다."),
    INVALID_REFRESH_TOKEN("U0004", HttpStatus.UNAUTHORIZED, "유효하지 않은 리프레시 토큰입니다."),
    EXPIRED_REFRESH_TOKEN("U0005", HttpStatus.UNAUTHORIZED, "만료된 리프레시 토큰입니다."),
    EMAIL_NOT_VERIFIED("U0006", HttpStatus.FORBIDDEN, "이메일 인증이 완료되지 않았습니다."),

    // notification-service -> N0001
    NOTIFICATION_NOT_FOUND("N0001", HttpStatus.NOT_FOUND, "존재하지 않는 알림입니다."),

    // payment-service -> P0001
    PAYMENT_NOT_FOUND("P0001", HttpStatus.NOT_FOUND, "존재하지 않는 결제 정보입니다."),
    PAYMENT_ALREADY_EXISTS("P0002", HttpStatus.CONFLICT, "이미 존재하는 주문 번호입니다."),
    PAYMENT_AMOUNT_MISMATCH("P0003", HttpStatus.BAD_REQUEST, "결제 금액이 일치하지 않습니다."),
    PAYMENT_MERCHANT_UID_MISMATCH("P0004", HttpStatus.BAD_REQUEST, "주문 번호가 일치하지 않습니다."),
    PAYMENT_NOT_PAID("P0005", HttpStatus.BAD_REQUEST, "결제 완료된 건만 취소할 수 있습니다."),
    PAYMENT_PORTONE_ERROR("P0006", HttpStatus.INTERNAL_SERVER_ERROR, "포트원 API 호출 중 오류가 발생했습니다."),
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
