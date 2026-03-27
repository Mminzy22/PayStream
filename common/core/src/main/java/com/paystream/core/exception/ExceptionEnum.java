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
    PRODUCT_STORE_MISMATCH("I0009", HttpStatus.BAD_REQUEST, "가게에 포함된 상품이 아닙니다."),
    INVENTORY_NOT_FOUND("I0010", HttpStatus.NOT_FOUND, "상품의 재고가 존재하지 않습니다."),
    INVALID_DATE_RANGE("I0011", HttpStatus.BAD_REQUEST, "체크인 날짜와 체크아웃 날짜가 바뀌었습니다. 다시 확인해주세요."),
    OUT_OF_BOOKING_PERIOD("I0012", HttpStatus.BAD_REQUEST, "현재 예약 가능 기간이 아닙니다."),
    INSUFFICIENT_STOCK("I0013", HttpStatus.CONFLICT, "재고가 부족한 날짜가 있습니다. 다시 확인해주세요."),
    OVER_STOCK_FLOW("I0013", HttpStatus.CONFLICT, "저장 가능한 최대 수량을 초과하였습니다."),
    ALREADY_RESERVED_BY_USER("I0014", HttpStatus.BAD_REQUEST, "이미 해당 사용자가 선점(예약 시도) 중인 상품입니다."),
    RESERVATION_EXPIRED("I0015", HttpStatus.BAD_REQUEST, "유휴시간을 초과했습니다."),

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
    PAYMENT_MISSING_AUTH_USER_ID("P0007", HttpStatus.BAD_REQUEST, "인증 사용자 헤더가 누락되었습니다."),
    PAYMENT_INVALID_AUTH_USER_ID("P0008", HttpStatus.BAD_REQUEST, "인증 사용자 헤더 형식이 올바르지 않습니다."),
    PAYMENT_PORTONE_API_SECRET_MISSING(
            "P0009", HttpStatus.INTERNAL_SERVER_ERROR, "포트원 API Secret이 설정되지 않았습니다."),
    PAYMENT_PORTONE_UNAUTHORIZED("P0010", HttpStatus.UNAUTHORIZED, "포트원 API 인증에 실패했습니다."),
    PAYMENT_PORTONE_BAD_REQUEST("P0011", HttpStatus.BAD_REQUEST, "포트원 API 요청 형식이 올바르지 않습니다."),
    PAYMENT_PORTONE_NOT_FOUND("P0012", HttpStatus.NOT_FOUND, "포트원 결제 정보를 찾을 수 없습니다."),
    PAYMENT_PORTONE_CANCEL_CONFLICT(
            "P0013", HttpStatus.CONFLICT, "포트원 결제 취소 처리 중입니다. 잠시 후 다시 시도해주세요."),

    // order-service -> O0001
    ORDER_NOT_FOUND("O0001", HttpStatus.NOT_FOUND, "존재하지 않는 주문입니다."),
    ORDER_ALREADY_PAID("O0002", HttpStatus.CONFLICT, "이미 결제 완료된 주문입니다."),
    ORDER_ALREADY_CANCELLED("O0003", HttpStatus.CONFLICT, "이미 취소된 주문입니다."),
    ORDER_INVALID_STATUS("O0004", HttpStatus.BAD_REQUEST, "해당 상태에서는 요청할 수 없습니다."),
    ORDER_MISSING_AUTH_USER_ID("O0005", HttpStatus.BAD_REQUEST, "인증 사용자 헤더가 누락되었습니다."),
    ORDER_INVALID_AUTH_USER_ID("O0006", HttpStatus.BAD_REQUEST, "인증 사용자 헤더 형식이 올바르지 않습니다."),
    ORDER_EVENT_PUBLISH_FAILED("O0007", HttpStatus.INTERNAL_SERVER_ERROR, "재고 선점 이벤트 발행에 실패했습니다."),

    USER_MISSING_BEARER_TOKEN("U0007", HttpStatus.BAD_REQUEST, "Authorization 헤더에 Bearer 토큰이 필요합니다."),
    USER_BLACKLIST_ADD_FAILED("U0008", HttpStatus.INTERNAL_SERVER_ERROR, "블랙리스트 추가에 실패했습니다."),
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
