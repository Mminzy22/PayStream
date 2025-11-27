package com.paystream.core.exception;

import java.util.stream.Collectors;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Order(Integer.MAX_VALUE)
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

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ExceptionResponse handleValidationExceptions(MethodArgumentNotValidException e) {

        // 오류가 발생한 모든 필드와 메시지를 추출하여 세미콜론(;)으로 연결
        String errorMessage =
                e.getBindingResult().getFieldErrors().stream()
                        .map(
                                error ->
                                        String.format(
                                                "[%s]: %s",
                                                error.getField(), error.getDefaultMessage()))
                        .collect(Collectors.joining("; "));

        // 유효성 검사 실패는 HTTP 400 Bad Request로 응답
        return ExceptionResponse.builder()
                .code(HttpStatus.BAD_REQUEST.toString())
                .status(HttpStatus.BAD_REQUEST)
                .message("유효성 검사 실패: " + errorMessage)
                .build();
    }
}
