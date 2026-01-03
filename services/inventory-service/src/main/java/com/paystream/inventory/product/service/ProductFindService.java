package com.paystream.inventory.product.service;

import static com.paystream.core.exception.ExceptionEnum.INVENTORY_NOT_FOUND;
import static com.paystream.core.exception.ExceptionEnum.PRODUCT_NOT_FOUND;

import com.paystream.core.exception.PayStreamException;
import com.paystream.inventory.inventory.dto.response.DailyInventoryResponse;
import com.paystream.inventory.product.dto.response.ProductDetailResponse;
import com.paystream.inventory.product.entity.Product;
import com.paystream.inventory.product.repository.ProductRepository;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Transactional(readOnly = true)
@Service
@RequiredArgsConstructor
public class ProductFindService {

    private final ProductRepository productRepository;

    public ProductDetailResponse findProductDetail(Long productId) {
        Product findProduct =
                productRepository
                        .findById(productId)
                        .orElseThrow(() -> new PayStreamException(PRODUCT_NOT_FOUND));

        return ProductDetailResponse.of(findProduct);
    }

    /**
     * 상품을 조회시 체크인 ~ 체크아웃 기간에 해당하는 상품의 재고를 함께 반환한다.
     *
     * @param productId
     * @param checkInDate
     * @param checkOutDate
     */
    public ProductDetailResponse findProductWithDailyInventory(
            Long productId, LocalDate checkInDate, LocalDate checkOutDate) {
        Product findProduct =
                productRepository
                        .findById(productId)
                        .orElseThrow(() -> new PayStreamException(PRODUCT_NOT_FOUND));

        List<DailyInventoryResponse> dailyInventories =
                findProduct.getDailyInventories().stream()
                        .filter(
                                inventory -> {
                                    LocalDate inventoryDate = inventory.getDate();
                                    return !inventoryDate.isBefore(checkInDate)
                                            && inventoryDate.isBefore(checkOutDate);
                                })
                        .map(DailyInventoryResponse::of)
                        .toList();

        if (dailyInventories.isEmpty()) {
            throw new PayStreamException(INVENTORY_NOT_FOUND);
        }

        return ProductDetailResponse.of(findProduct, dailyInventories);
    }
}
