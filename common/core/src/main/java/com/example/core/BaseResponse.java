package com.example.core;

import jakarta.annotation.Nullable;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@NoArgsConstructor
public final class BaseResponse<T> {

    private int code; // HTTP 상태 코드 번호
    private HttpStatus status;
    private String message;
    private T data;

    // ⭐️ private 생성자 하나만 사용
    private BaseResponse(HttpStatus status, String message, @Nullable T data) {
        this.code = status.value();
        this.status = status;
        this.message = message;
        this.data = data;
    }

    // --- 정적 팩토리 메서드 ---

    // 1. 상태 + 메시지 + 데이터 (가장 일반적)
    public static <T> BaseResponse<T> of(HttpStatus status, String message, @Nullable T data) {
        return new BaseResponse<>(status, message, data);
    }

    // 2. 상태 + 데이터 (메시지는 상태 이름으로 자동 설정)
    public static <T> BaseResponse<T> of(HttpStatus status, @Nullable T data) {
        return new BaseResponse<>(status, status.name(), data);
    }

    // 3. 성공 (200 OK)
    public static <T> BaseResponse<T> ok(@Nullable T data) {
        return of(HttpStatus.OK, data);
    }

    // 4. 생성 완료 (201 CREATED)
    public static <T> BaseResponse<T> created(@Nullable T data) {
        return of(HttpStatus.CREATED, data);
    }
}
