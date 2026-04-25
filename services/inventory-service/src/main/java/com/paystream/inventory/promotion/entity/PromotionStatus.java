package com.paystream.inventory.promotion.entity;

import static com.paystream.core.exception.ExceptionEnum.INVALID_STATUS_VALUE;

import com.paystream.core.exception.PayStreamException;
import org.springframework.util.StringUtils;

public enum PromotionStatus {
    ACTIVE,
    DISABLED,
    FINISHED;

    public static PromotionStatus getStatus(String status) {
        if (!StringUtils.hasText(status)) {
            return null;
        }

        try {
            return PromotionStatus.valueOf(status.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new PayStreamException(INVALID_STATUS_VALUE);
        }
    }
}
