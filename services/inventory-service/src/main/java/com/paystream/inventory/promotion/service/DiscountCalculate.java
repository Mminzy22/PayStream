package com.paystream.inventory.promotion.service;

import com.paystream.inventory.promotion.dto.response.DiscountResult;
import com.paystream.inventory.promotion.entity.DiscountType;
import com.paystream.inventory.promotion.entity.Promotion;
import java.util.List;
import java.util.OptionalDouble;
import java.util.stream.Stream;
import org.springframework.stereotype.Component;

@Component
public class DiscountCalculate {

    public DiscountResult calculateBestDiscount(
            long originPrice, List<Promotion> storePromos, List<Promotion> productPromos) {
        // 모든 프로모션 중 최대 할인액 찾기
        long maxDiscountAmount =
                Stream.concat(storePromos.stream(), productPromos.stream())
                        .mapToLong(p -> calculateAmount(originPrice, p))
                        .max()
                        .orElse(0L);

        // 할인 결과 생성
        return createDiscountResult(originPrice, maxDiscountAmount);
    }

    /**
     * 항닝 결과 객체 생성 (검증 및 소수점 처리 포함)
     *
     * @param originPrice
     * @param discountAmount
     * @return
     */
    public static DiscountResult createDiscountResult(long originPrice, long discountAmount) {
        if (originPrice <= 0 || discountAmount <= 0) {
            return new DiscountResult(originPrice, originPrice, 0.0, 0L);
        }

        long finalPrice = Math.max(0, originPrice - discountAmount);

        // 할인율 계산 및 소수점 첫째 자리 반올림 (15.56 -> 15.6)
        double rawRate = ((double) discountAmount / originPrice) * 100;
        double discountRate = Math.round(rawRate * 10.0) / 10.0;

        return new DiscountResult(originPrice, finalPrice, discountRate, discountAmount);
    }

    /**
     * 할인되는 금액 계산
     *
     * @param price 원래 숙소의 가격
     * @param promotion 프로모션
     * @return 할인되는 금액
     */
    public static long calculateAmount(long price, Promotion promotion) {
        if (promotion.getDiscountType() == DiscountType.PERCENT) {
            // 할인액 계산시 반올림 처리
            return Math.round(price * (promotion.getDiscountValue() / 100.0));
        }
        // 정액 할인의 경우 원가를 넘지 않도록 제한
        return Math.min(price, promotion.getDiscountValue()); // FIXED_AMOUNT
    }

    /**
     * 할인 금액 평균값 계산
     *
     * @param history
     * @return
     */
    public static double calculateAverageDiscount(List<DiscountResult> history) {
        // 평균 금액 계산
        OptionalDouble average =
                history.stream().mapToLong(DiscountResult::getDiscountedPrice).average();

        // 값이 없을 경우를 대비해 기본값 설정
        return average.orElse(0.0);
    }
}
