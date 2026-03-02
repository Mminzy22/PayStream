package com.paystream.inventory.promotion.entity;

import com.paystream.core.BaseEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import java.time.LocalDate;
import lombok.*;

@ToString
@Entity
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class Promotion extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;
    private TargetType targetType; // STORE, PRODUCT
    private Long targetId; // 가게ID 또는 방ID
    private DiscountType discountType; // PERCENT / FIXED_AMOUNT
    private int discountValue; // 10% 또는 5000원
    private LocalDate startDate;
    private LocalDate endDate;
}
