package com.paystream.inventory.product.service;

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

    /** 숙소의 세일 내역을 포함한 상세 데이터 조회 */
    public List<ProductPriceHistoryResponse> getPriceHistoryDetails(Long productId) {
        Product findProduct =
                productRepository
                        .findById(productId)
                        .orElseThrow(() -> new PayStreamException(ExceptionEnum.PRODUCT_NOT_FOUND));

        // 조회한 숙소id로 promotion을 조회하고, 적용됐던 할인 이력을 가져온다.
        List<Promotion> targetPromotionList =
                promotionRepository.findByTargetId(findProduct.getId());

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
