package com.paystream.inventory.product.service;

import static com.paystream.core.exception.ExceptionEnum.DATE_EXCEEDS_MAX_RANGE;
import static com.paystream.core.exception.ExceptionEnum.INVALID_DATE_RANGE;
import static com.paystream.inventory.promotion.service.DiscountCalculate.calculateAmount;
import static com.paystream.inventory.promotion.service.DiscountCalculate.createDiscountResult;

import com.paystream.core.exception.ExceptionEnum;
import com.paystream.core.exception.PayStreamException;
import com.paystream.inventory.product.dto.response.ProductPriceHistoryResponse;
import com.paystream.inventory.product.entity.Product;
import com.paystream.inventory.product.repository.ProductRepository;
import com.paystream.inventory.promotion.dto.response.DiscountResult;
import com.paystream.inventory.promotion.entity.Promotion;
import com.paystream.inventory.promotion.repository.PromotionRepository;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Transactional(readOnly = true)
@Service
@RequiredArgsConstructor
public class ProductPriceHistoryService {

    private final ProductRepository productRepository;
    private final PromotionRepository promotionRepository;

    // 날짜를 최대 1년로 설정해서 1개월, 3개월, 6개월, 1년까지 조회가능. 날짜는 커스텀이 가능.
    /** 숙소의 세일 내역을 포함한 상세 데이터 조회 */
    public List<ProductPriceHistoryResponse> getPriceHistoryDetails(
            Long productId, LocalDate startDate, LocalDate endDate) {
        if (!startDate.isBefore(endDate)) {
            throw new PayStreamException(INVALID_DATE_RANGE);
        }

        // 오늘 기준 최대 1년 전까지만 조회
        LocalDate nowBeforeOneYears = LocalDate.now().minusYears(1);
        if (startDate.isBefore(nowBeforeOneYears)) {
            throw new PayStreamException(DATE_EXCEEDS_MAX_RANGE);
        }

        Product findProduct =
                productRepository
                        .findById(productId)
                        .orElseThrow(() -> new PayStreamException(ExceptionEnum.PRODUCT_NOT_FOUND));

        // 조회한 숙소id로 promotion을 조회하고, 적용됐던 할인 이력을 가져온다.
        List<Promotion> targetPromotionList =
                promotionRepository.findByProductTargetIdBetweenDate(
                        findProduct.getId(), startDate, endDate);

        if (targetPromotionList.isEmpty()) {
            throw new PayStreamException(ExceptionEnum.PROMOTION_NOT_FOUND);
        }

        // 숙소의 할인 내역 리스트 조회
        // 시작일, 종료일, 원가, 최종가, 할인율 계산
        return calculateProductDiscount(findProduct.getBasePrice(), targetPromotionList);
    }

    /**
     * 해당 숙소의 할인 정보를 리스트로 조회
     *
     * @param originPrice
     * @param productPromos
     * @return
     */
    private List<ProductPriceHistoryResponse> calculateProductDiscount(
            long originPrice, List<Promotion> productPromos) {
        return productPromos.stream()
                .map(
                        promo -> {
                            long discountPrice = calculateAmount(originPrice, promo);
                            DiscountResult result =
                                    createDiscountResult(originPrice, discountPrice);

                            return ProductPriceHistoryResponse.of(promo, result);
                        })
                .toList();
    }
}
