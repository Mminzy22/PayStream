package com.paystream.inventory.promotion.service;

import static com.paystream.core.exception.ExceptionEnum.*;

import com.paystream.core.exception.PayStreamException;
import com.paystream.inventory.promotion.dto.request.PromotionRequest;
import com.paystream.inventory.promotion.entity.DiscountType;
import com.paystream.inventory.promotion.entity.Promotion;
import com.paystream.inventory.promotion.entity.PromotionStatus;
import com.paystream.inventory.promotion.entity.TargetType;
import com.paystream.inventory.promotion.repository.PromotionRepository;
import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class PromotionService {
    private final PromotionRepository promotionRepository;

    public Long create(PromotionRequest request) {
        // 기간 역전 검증
        if (validatePeriod(request.getStartDate(), request.getEndDate())) {
            throw new PayStreamException(PROMOTION_NOT_PERIOD);
        }

        // 할인율 범위 제한 확인
        if (!validateDiscountRate(request.getDiscountType(), request.getDiscountValue())) {
            throw new PayStreamException(PROMOTION_NOT_RATE);
        }

        // 중복된 프로모션이 있는지 검증
        if (validateDuplicate(
                request.getTargetType(),
                request.getTargetId(),
                request.getStartDate(),
                request.getEndDate())) {
            throw new PayStreamException(PROMOTION_DUPLICATE);
        }
        Promotion promotion = request.toEntity();
        promotion.updateStatus(PromotionStatus.ACTIVE);
        return promotionRepository.save(promotion).getId();
    }

    public Long update(Long id, String currentUserId, PromotionRequest request) {
        // 할인율 범위 제한 확인
        if (!validateDiscountRate(request.getDiscountType(), request.getDiscountValue())) {
            throw new PayStreamException(PROMOTION_NOT_RATE);
        }

        Promotion promotion =
                promotionRepository
                        .findById(id)
                        .orElseThrow(() -> new PayStreamException(PROMOTION_NOT_FOUND));

        if (currentUserId.equals(promotion.getCreateUserId())) {
            throw new PayStreamException(IS_NOT_CREATE_USER);
        }

        promotion.update(request);

        return promotion.getId();
    }

    // 프로모션 종료
    public void finished(Long id, String currentUserId) {
        Promotion promotion =
                promotionRepository
                        .findById(id)
                        .orElseThrow(() -> new PayStreamException(PROMOTION_NOT_FOUND));

        if (currentUserId.equals(promotion.getCreateUserId())) {
            throw new PayStreamException(IS_NOT_CREATE_USER);
        }

        promotion.updateStatus(PromotionStatus.FINISHED);
    }

    // 기간 역전 검증
    private boolean validatePeriod(LocalDate startDate, LocalDate endDate) {
        return startDate.isAfter(endDate);
    }

    // 할인율 범위 제한
    private boolean validateDiscountRate(String discountType, int discountValue) {
        // 할인 방식이 퍼센트가 아닐 경우 그냥 통과
        if (discountType.equals(DiscountType.FIXED_AMOUNT.name())) {
            return discountValue >= 0;
        }

        return discountValue >= 0 && discountValue <= 100;
    }

    // 중복된 프로모션 상품이 있는지 검증
    private boolean validateDuplicate(
            String targetType, Long targetId, LocalDate startDate, LocalDate endDate) {

        TargetType type = null;
        try {
            type = TargetType.valueOf(targetType);
        } catch (IllegalArgumentException e) {
            throw new PayStreamException(PROMOTION_TYPE_DOES_NOT_EXIST);
        }

        return promotionRepository.existsOverlappingPromotion(type, targetId, startDate, endDate);
    }
}
