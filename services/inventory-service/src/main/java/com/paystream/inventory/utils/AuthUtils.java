package com.paystream.inventory.utils;

import static com.paystream.core.exception.ExceptionEnum.ACCESS_DENIED_EXCEPTION;

import com.paystream.core.exception.PayStreamException;
import jakarta.servlet.http.HttpServletRequest;

public class AuthUtils {

    public static String getCurrentUserId(HttpServletRequest request) {
        String hostId = request.getHeader("X-Auth-User-Id");

        if (hostId == null || hostId.isEmpty()) {
            throw new PayStreamException(ACCESS_DENIED_EXCEPTION);
        }

        return hostId;
    }
}
