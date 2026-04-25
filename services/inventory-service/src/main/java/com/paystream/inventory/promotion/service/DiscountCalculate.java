package com.paystream.inventory.promotion.service;

import com.paystream.inventory.promotion.dto.response.DiscountResult;
import com.paystream.inventory.promotion.entity.DiscountType;
import com.paystream.inventory.promotion.entity.Promotion;
import java.util.List;
import java.util.stream.Stream;
import org.springframework.stereotype.Component;

@Component
public class DiscountCalculate {

    public DiscountResult calculateBestDiscount(
            long originPrice, List<Promotion> storePromos, List<Promotion> productPromos) {
        // 1. 모든 프로모션 중 최대 할인액 찾기
        long maxDiscountAmount =
                Stream.concat(storePromos.stream(), productPromos.stream())
                        .mapToLong(p -> calculateAmount(originPrice, p))
                        .max()
                        .orElse(0L);

        if (maxDiscountAmount <= 0) {
            return DiscountResult.noDiscount(originPrice);
        }

        // 2. 최종 가격 및 할인율 계산
        long finalPrice = Math.max(0, originPrice - maxDiscountAmount);
        int discountRate = (int) (((double) maxDiscountAmount / originPrice) * 100);

        return new DiscountResult(originPrice, finalPrice, discountRate, maxDiscountAmount);
    }

    /**
     * 할인율 적용 가격 계산
     *
     * @param price 할인 전 가격
     * @param promotion 프로모션
     * @return 할인이 적용된 가격
     */
    private long calculateAmount(long price, Promotion promotion) {
        if (promotion.getDiscountType() == DiscountType.PERCENT) {
            return (long) (price * (promotion.getDiscountValue() / 100.0));
        }
        return promotion.getDiscountValue(); // FIXED_AMOUNT
    }
}
