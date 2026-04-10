package com.paystream.inventory.product.service;

import static com.paystream.core.exception.ExceptionEnum.INVENTORY_NOT_FOUND;
import static com.paystream.core.exception.ExceptionEnum.PRODUCT_NOT_FOUND;

import com.paystream.core.exception.PayStreamException;
import com.paystream.inventory.inventory.dto.response.DailyInventoryResponse;
import com.paystream.inventory.photo.service.StorageService;
import com.paystream.inventory.product.dto.response.ProductDetailResponse;
import com.paystream.inventory.product.dto.response.ProductResponse;
import com.paystream.inventory.product.entity.Product;
import com.paystream.inventory.product.repository.ProductRepository;
import com.paystream.inventory.promotion.dto.response.DiscountResult;
import com.paystream.inventory.promotion.entity.Promotion;
import com.paystream.inventory.promotion.entity.TargetType;
import com.paystream.inventory.promotion.repository.PromotionRepository;
import com.paystream.inventory.promotion.service.DiscountCalculate;
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
    private final StorageService storageService;
    private final PromotionRepository promotionRepository;
    private final DiscountCalculate discountCalculate;

    /**
     * 상품 정보 조회
     *
     * @param productId 상품 번호
     * @return 상품 정보
     */
    public ProductDetailResponse findProductDetail(Long productId) {
        Product findProduct =
                productRepository
                        .findById(productId)
                        .orElseThrow(() -> new PayStreamException(PRODUCT_NOT_FOUND));

        // 이미지 파일명 리스트를 URL 리스트로 변환
        List<String> imageUrls =
                findProduct.getPhotos().stream()
                        .map(photo -> storageService.getImageUrl(photo.getFileName()))
                        .toList();

        // 할인율 계산
        DiscountResult discount = getDiscountResult(findProduct);

        return ProductDetailResponse.of(findProduct, imageUrls, discount);
    }

    private static List<Promotion> getPromotions(
            List<Promotion> allPromotions, TargetType product) {
        return allPromotions.stream().filter(p -> p.getTargetType() == product).toList();
    }

    /**
     * 상품을 조회시 체크인 ~ 체크아웃 기간에 해당하는 상품의 재고를 함께 반환한다.
     *
     * @param productId 상품 아이디
     * @param checkInDate 체크인 날짜
     * @param checkOutDate 체크아웃 날짜
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

        // 이미지 URL 변환
        List<String> imageUrls =
                findProduct.getPhotos().stream()
                        .map(photo -> storageService.getImageUrl(photo.getFileName()))
                        .toList();

        // 할인율 계산
        DiscountResult discount = getDiscountResult(findProduct);

        return ProductDetailResponse.of(findProduct, dailyInventories, imageUrls, discount);
    }

    /**
     * 유저가 등록한 가게의 기본적인 상품 리스트 조회 (단순 조회용)
     *
     * @param userId
     * @return 유저가 갖고 있는 상품들을 조회
     */
    public List<ProductResponse> listUserProducts(String userId) {
        List<Product> findAllUserProduct = productRepository.findAllByStore_HostId(userId);

        return findAllUserProduct.stream().map(ProductResponse::from).toList();
    }

    // 할인율 계산
    private DiscountResult getDiscountResult(Product findProduct) {
        List<Promotion> allPromotions =
                promotionRepository.findAllValidPromotions(
                        List.of(findProduct.getStore().getId()), List.of(findProduct.getId()));

        List<Promotion> currentStorePromo = getPromotions(allPromotions, TargetType.STORE);
        List<Promotion> currentProductPromo = getPromotions(allPromotions, TargetType.PRODUCT);

        return discountCalculate.calculateBestDiscount(
                findProduct.getBasePrice(), currentStorePromo, currentProductPromo);
    }
}
