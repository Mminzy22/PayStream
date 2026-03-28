package com.paystream.inventory.promotion.service;

import static com.paystream.core.exception.ExceptionEnum.PROMOTION_NOT_FOUND;

import com.paystream.core.exception.PayStreamException;
import com.paystream.inventory.promotion.dto.request.PromotionFindRequest;
import com.paystream.inventory.promotion.dto.response.PromotionResponse;
import com.paystream.inventory.promotion.entity.Promotion;
import com.paystream.inventory.promotion.entity.PromotionStatus;
import com.paystream.inventory.promotion.repository.PromotionRepository;
import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

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

    public Page<PromotionResponse> findAll(PromotionFindRequest request, Pageable reqPageable) {
        int pageNumber = Math.max(0, reqPageable.getPageNumber() - 1);
        Pageable pageable = PageRequest.of(pageNumber, reqPageable.getPageSize());

        // 이름, 기간, 할인 범위, 행사 상태
        String title = request.getTitle();
        String reqStatus = request.getStatus();
        LocalDate startDate = request.getStartDate();
        LocalDate endDate = request.getEndDate();

        // 프로모션 상태 가져오기
        PromotionStatus status = null;
        if (StringUtils.hasText(reqStatus)) {
            status = PromotionStatus.getStatus(reqStatus);
        }

        Page<Promotion> promotions =
                promotionRepository.findAllPromotionWithPaging(
                        title, status, startDate, endDate, pageable);

        return promotions.map(PromotionResponse::of);
    }
}
