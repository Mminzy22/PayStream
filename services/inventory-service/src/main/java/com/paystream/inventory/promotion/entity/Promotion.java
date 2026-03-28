package com.paystream.inventory.promotion.entity;

import com.paystream.core.BaseEntity;
import com.paystream.inventory.promotion.dto.request.PromotionRequest;
import jakarta.persistence.*;
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

    @Column(nullable = false)
    private String createUserId; // 생성자 ID

    private String title;

    @Enumerated(EnumType.STRING)
    private TargetType targetType; // STORE, PRODUCT

    private Long targetId; // 가게ID 또는 방ID

    @Enumerated(EnumType.STRING)
    private DiscountType discountType; // PERCENT / FIXED_AMOUNT

    private int discountValue; // 10% 또는 5000원
    private LocalDate startDate;
    private LocalDate endDate;

    @Enumerated(EnumType.STRING)
    private PromotionStatus status;

    public void update(PromotionRequest request) {
        this.title = request.getTitle();
        this.targetType = TargetType.valueOf(request.getTargetType());
        this.targetId = request.getTargetId();
        this.discountType = DiscountType.valueOf(request.getDiscountType());
        this.discountValue = request.getDiscountValue();
        this.startDate = request.getStartDate();
        this.endDate = request.getEndDate();
    }

    public void updateStatus(PromotionStatus status) {
        this.status = status;
    }
}
