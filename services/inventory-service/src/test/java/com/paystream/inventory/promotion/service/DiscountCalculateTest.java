package com.paystream.inventory.promotion.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

import com.paystream.inventory.promotion.dto.response.DiscountResult;
import com.paystream.inventory.promotion.entity.DiscountType;
import com.paystream.inventory.promotion.entity.Promotion;
import com.paystream.inventory.promotion.entity.TargetType;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class DiscountCalculateTest {

    private final DiscountCalculate discountCalculate = new DiscountCalculate();

    @Test
    @DisplayName("가게 할인과 상품 할인 중 더 큰 금액이 적용되어야 한다")
    void calculateBestDiscountTest() {
        // given
        long originPrice = 200_000L;

        // 가게 할인: 10% (금액 환산 시 20,000원)
        Promotion storePromo =
                Promotion.builder()
                        .targetType(TargetType.STORE)
                        .discountType(DiscountType.PERCENT)
                        .discountValue(10)
                        .build();

        // 상품 할인: 30,000원 (정액)
        Promotion productPromo =
                Promotion.builder()
                        .targetType(TargetType.PRODUCT)
                        .discountType(DiscountType.FIXED_AMOUNT)
                        .discountValue(30_000)
                        .build();

        // when
        DiscountResult result =
                discountCalculate.calculateBestDiscount(
                        originPrice, List.of(storePromo), List.of(productPromo));

        // then
        assertThat(result.getDiscountedPrice()).isEqualTo(170_000L); // 20만 - 3만
        assertThat(result.getDiscountRate()).isEqualTo(15); // 3만 / 20만 * 100
        assertThat(result.getDiscountRate() > 0).isTrue();
    }
}
