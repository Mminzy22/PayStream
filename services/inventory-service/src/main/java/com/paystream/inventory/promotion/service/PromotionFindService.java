package com.paystream.inventory.promotion.service;

import static com.paystream.core.exception.ExceptionEnum.PROMOTION_NOT_FOUND;

import com.paystream.core.exception.PayStreamException;
import com.paystream.inventory.promotion.dto.response.PromotionResponse;
import com.paystream.inventory.promotion.entity.Promotion;
import com.paystream.inventory.promotion.repository.PromotionRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PromotionFindService {
    private final PromotionRepository promotionRepository;

    public PromotionResponse findById(Long id) {
        Promotion promotion =
                promotionRepository
                        .findById(id)
                        .orElseThrow(() -> new PayStreamException(PROMOTION_NOT_FOUND));

        return PromotionResponse.of(promotion);
    }

    // 필터링 구현시 추가적인 로직 필요
    // 페이징 구현
    public List<PromotionResponse> findAll() {
        List<Promotion> promotions = promotionRepository.findAll();

        return promotions.stream().map(PromotionResponse::of).toList();
    }
}
