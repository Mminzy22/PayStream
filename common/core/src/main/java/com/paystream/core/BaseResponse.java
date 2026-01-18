package com.paystream.core;

import jakarta.annotation.Nullable;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@NoArgsConstructor
public class BaseResponse<T> {

    private int code;
    private HttpStatus status;
    private String message;
    private T data;

    private BaseResponse(HttpStatus status, String message, T data) {
        this.code = status.value();
        this.status = status;
        this.message = message;
        this.data = data;
    }

    public static <T> BaseResponse<T> of(HttpStatus status, String message, T data) {
        return new BaseResponse<>(status, message, data);
    }

    public static <T> BaseResponse<T> of(HttpStatus status, T data) {
        return new BaseResponse<>(status, status.name(), data);
    }

    public static <T> BaseResponse<T> ok(T data) {
        return of(HttpStatus.OK, data);
    }

    public static <T> BaseResponse<T> created(@Nullable T data) {
        return of(HttpStatus.CREATED, data);
    }

    public static <T> BaseResponse<T> error(int code, String message) {
        return of(HttpStatus.valueOf(code), message, null);
    }

    public static <T> BaseResponse<T> error(HttpStatus status, String message) {
        return of(status, message, null);
    }
}
