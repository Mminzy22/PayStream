package com.paystream.payment.util;

import com.paystream.core.exception.ExceptionEnum;
import com.paystream.core.exception.PayStreamException;
import jakarta.servlet.http.HttpServletRequest;

/** HTTP 요청 관련 유틸리티 */
public class RequestUtil {

    /**
     * 현재 로그인한 사용자 ID 가져오기 API Gateway에서 설정한 X-Auth-User-Id 헤더에서 사용자 ID를 추출합니다.
     *
     * @param request HTTP 요청
     * @return 사용자 ID
     * @throws PayStreamException 헤더가 없거나 유효하지 않은 경우
     */
    public static Long getCurrentUserId(HttpServletRequest request) {
        String userIdHeader = request.getHeader("X-Auth-User-Id");
        if (userIdHeader == null) {
            throw new PayStreamException(ExceptionEnum.PAYMENT_MISSING_AUTH_USER_ID);
        }

        try {
            return Long.parseLong(userIdHeader);
        } catch (NumberFormatException e) {
            throw new PayStreamException(ExceptionEnum.PAYMENT_INVALID_AUTH_USER_ID);
        }
    }
}
