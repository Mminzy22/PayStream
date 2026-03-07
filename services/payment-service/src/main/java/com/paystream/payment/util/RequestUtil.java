package com.paystream.payment.util;

import jakarta.servlet.http.HttpServletRequest;

/** HTTP 요청 관련 유틸리티 */
public class RequestUtil {

    /**
     * 현재 로그인한 사용자 ID 가져오기 API Gateway에서 설정한 X-Auth-User-Id 헤더에서 사용자 ID를 추출합니다.
     *
     * @param request HTTP 요청
     * @return 사용자 ID
     * @throws RuntimeException 헤더가 없거나 유효하지 않은 경우
     */
    public static Long getCurrentUserId(HttpServletRequest request) {
        String userIdHeader = request.getHeader("X-Auth-User-Id");
        if (userIdHeader == null) {
            throw new RuntimeException("인증된 사용자 정보를 찾을 수 없습니다. API Gateway를 통해 요청해야 합니다.");
        }

        try {
            return Long.parseLong(userIdHeader);
        } catch (NumberFormatException e) {
            throw new RuntimeException("유효하지 않은 사용자 ID입니다: " + userIdHeader);
        }
    }
}
