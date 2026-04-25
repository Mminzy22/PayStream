package com.paystream.inventory.store.service;

import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toList;

import com.paystream.core.exception.ExceptionEnum;
import com.paystream.core.exception.PayStreamException;
import com.paystream.inventory.annotation.CacheKeyParam;
import com.paystream.inventory.config.PageResponse;
import com.paystream.inventory.inventory.entity.DailyInventory;
import com.paystream.inventory.inventory.repository.DailyInventoryRepository;
import com.paystream.inventory.photo.service.StorageService;
import com.paystream.inventory.product.dto.response.ProductResponse;
import com.paystream.inventory.product.entity.Product;
import com.paystream.inventory.product.repository.ProductRepository;
import com.paystream.inventory.promotion.dto.response.DiscountResult;
import com.paystream.inventory.promotion.entity.Promotion;
import com.paystream.inventory.promotion.entity.TargetType;
import com.paystream.inventory.promotion.repository.PromotionRepository;
import com.paystream.inventory.promotion.service.DiscountCalculate;
import com.paystream.inventory.store.dto.request.StoreFindRequest;
import com.paystream.inventory.store.dto.request.StoreListFindRequest;
import com.paystream.inventory.store.dto.response.StoreBaseResponse;
import com.paystream.inventory.store.dto.response.StoreResponse;
import com.paystream.inventory.store.entity.Store;
import com.paystream.inventory.store.repository.StoreQueryDslRepository;
import com.paystream.inventory.store.repository.StoreRepository;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Transactional(readOnly = true)
@Slf4j
@Service
@RequiredArgsConstructor
public class StoreFindService {

    private final StoreRepository storeRepository;
    private final StorageService storageService;
    private final ProductRepository productRepository;
    private final StoreQueryDslRepository storeQueryDslRepository;
    private final DailyInventoryRepository dailyInventoryRepository;
    private final PromotionRepository promotionRepository;
    private final DiscountCalculate discountCalculate;

    // 조회 성능 향상을 위한 Redis 캐싱 기능 추가하기
    @Cacheable(value = "stores", keyGenerator = "customKeyGenerator")
    public PageResponse<StoreResponse> userFindStoreList(
            @CacheKeyParam StoreListFindRequest request, Pageable reqPageable) {
        int pageNumber = Math.max(0, reqPageable.getPageNumber() - 1);
        Pageable pageable = PageRequest.of(pageNumber, reqPageable.getPageSize());

        // 가게 전체 조회
        Page<Store> stores = storeQueryDslRepository.findAllByFetchJoin(request, pageable);

        if (stores.isEmpty()) {
            // 빈값이면 비어 있는 상태로 반환
            Page<StoreResponse> emptyResponse = stores.map(StoreResponse::of);
            return new PageResponse<>(emptyResponse);
        }

        List<Long> storeIds = stores.getContent().stream().map(Store::getId).toList();

        // 예약 가능한 상품들 일괄 조회 (그룹화)
        List<Product> allAvailableProducts =
                productRepository.findAvailableProducts(
                        storeIds, request.getCheckInDate(), request.getCheckOutDate());

        Map<Long, List<Product>> productsByStoreId =
                allAvailableProducts.stream().collect(groupingBy(p -> p.getStore().getId()));

        // 현재 페이지의 모든 상품 ID 수집 및 프로모션 일괄 조회
        List<Long> allProductIds = allAvailableProducts.stream().map(Product::getId).toList();

        List<Promotion> allPromotions =
                promotionRepository.findAllValidPromotions(storeIds, allProductIds);

        // 프로모션을 Type별, TargetId별로 매핑
        Map<Long, List<Promotion>> storePromotionMap =
                allPromotions.stream()
                        .filter(p -> p.getTargetType() == TargetType.STORE)
                        .collect(groupingBy(Promotion::getTargetId));

        Map<Long, List<Promotion>> productPromotionMap =
                allPromotions.stream()
                        .filter(p -> p.getTargetType() == TargetType.PRODUCT)
                        .collect(groupingBy(Promotion::getTargetId));

        Page<StoreResponse> responsePage =
                stores.map(
                        store -> {
                            List<Product> storeProducts =
                                    productsByStoreId.getOrDefault(
                                            store.getId(), Collections.emptyList());
                            List<Promotion> currentStoreProms =
                                    storePromotionMap.getOrDefault(
                                            store.getId(), Collections.emptyList());

                            // 가게별 최저금액에 해당하는 상품 찾기
                            ProductResponse bestProduct =
                                    storeProducts.stream()
                                            .map(
                                                    p -> {
                                                        List<Promotion> currentProductPromos =
                                                                productPromotionMap.getOrDefault(
                                                                        p.getId(),
                                                                        Collections.emptyList());
                                                        // 할인율 미리 계산
                                                        DiscountResult discount =
                                                                discountCalculate
                                                                        .calculateBestDiscount(
                                                                                p.getBasePrice(),
                                                                                currentStoreProms,
                                                                                currentProductPromos);

                                                        return new Object() { // 임시객체
                                                            final Product product = p;
                                                            final DiscountResult discountResult =
                                                                    discount;
                                                        };
                                                    })
                                            .min(
                                                    Comparator.comparingLong(
                                                            pair ->
                                                                    pair.discountResult
                                                                            .getDiscountedPrice())) // 할인된 가격으로 최저가 비교
                                            .map(
                                                    pair -> {
                                                        Product p = pair.product;
                                                        DiscountResult dr = pair.discountResult;

                                                        // 예약 가능 여부 확인
                                                        boolean isAvailable =
                                                                getIsAvailable(
                                                                        request.getPersonCount(),
                                                                        p,
                                                                        p.getDailyInventories());

                                                        // 이미지 URL 변환
                                                        List<String> imageUrls = getImageUrls(p);

                                                        return ProductResponse.of(
                                                                p,
                                                                isAvailable,
                                                                imageUrls,
                                                                dr.getOriginPrice(), // 원가
                                                                dr.getDiscountedPrice(), // 최종가
                                                                dr.getDiscountRate() // 할인율
                                                                );
                                                    })
                                            .orElse(null);

                            return StoreResponse.of(store, bestProduct);
                        });

        return new PageResponse<>(responsePage);
    }

    /**
     * 가게 상세 조회
     *
     * @param id
     * @param request
     * @return StoreResponse
     */
    public StoreResponse findStore(Long id, StoreFindRequest request) {
        Store store =
                storeQueryDslRepository
                        .findOne(id, request.getCheckInDate(), request.getCheckOutDate())
                        .orElseThrow(() -> new PayStreamException(ExceptionEnum.STORE_NOT_FOUND));

        List<Product> products = store.getProducts();
        List<Long> productIds = products.stream().map(Product::getId).toList();

        // 재고 정보 일괄 조회 및 매핑
        Map<Long, List<DailyInventory>> inventoriesMap =
                dailyInventoryRepository.findByProductIdIn(productIds).stream()
                        .collect(groupingBy(d -> d.getProduct().getId(), toList()));

        // 관련 프로모션 일괄 조회
        List<Promotion> allPromotions =
                promotionRepository.findAllValidPromotions(List.of(id), productIds);

        // 프로모션 메모리 매핑
        List<Promotion> currentStorePromos =
                allPromotions.stream().filter(p -> p.getTargetType() == TargetType.STORE).toList();

        Map<Long, List<Promotion>> productPromoMap =
                allPromotions.stream()
                        .filter(p -> p.getTargetType() == TargetType.PRODUCT)
                        .collect(groupingBy(Promotion::getTargetId));

        // 상품 응답 객체 생성
        List<ProductResponse> productResponses =
                products.stream()
                        .map(
                                p -> {
                                    List<DailyInventory> findInventory =
                                            inventoriesMap.getOrDefault(
                                                    p.getId(), Collections.emptyList());
                                    List<Promotion> currentProductPromos =
                                            productPromoMap.getOrDefault(
                                                    p.getId(), Collections.emptyList());

                                    // 예약 가능 여부 확인
                                    boolean isAvailable =
                                            getIsAvailable(
                                                    request.getPersonCount(), p, findInventory);

                                    // 이미지 URL 변환
                                    List<String> imageUrls = getImageUrls(p);

                                    // 할인 계산 (원가, 최종가, 할인율 포함한 객체)
                                    DiscountResult dr =
                                            discountCalculate.calculateBestDiscount(
                                                    p.getBasePrice(),
                                                    currentStorePromos,
                                                    currentProductPromos);

                                    return ProductResponse.of(
                                            p,
                                            isAvailable,
                                            imageUrls,
                                            dr.getOriginPrice(), // 원가
                                            dr.getDiscountedPrice(), // 최종가
                                            dr.getDiscountRate() // 할인율
                                            );
                                })
                        .toList();

        return StoreResponse.ofWithProducts(store, productResponses);
    }

    /**
     * 유저가 등록한 가게들의 리스트로 조회
     *
     * @param request
     * @param reqPageable
     * @return 유자가 갖고 있는 가게들을 Page 형식으로 조회
     */
    public PageResponse<StoreResponse> listUserStores(
            String userId, StoreListFindRequest request, Pageable reqPageable) {
        // 소유 가게만 조회
        request.setOwnerId(userId);

        return this.userFindStoreList(request, reqPageable);
    }

    /**
     * 유저가 갖고 있는 가게들의 기본정보 리스트 조회 (단순 조회용)
     *
     * @param userId
     * @return 유저가 갖고 있는 가게들을 조회
     */
    public List<StoreBaseResponse> getUserStoreList(String userId) {
        List<Store> findAllUserStore = storeRepository.findAllByHostId(userId);

        return findAllUserStore.stream().map(StoreBaseResponse::from).toList();
    }

    // 예약 가능 여부 확인
    private boolean getIsAvailable(int personCount, Product p, List<DailyInventory> inventory) {
        boolean isStock = inventory.stream().allMatch(inv -> inv.getStockAvailable() > 0);

        // 최대 수용인원과 재고가 없을 경우 체크 (예약 가능 상품이면 true)
        return isStock && personCount <= p.getMaxPersonCount();
    }

    // 이미지 URL 변환
    private List<String> getImageUrls(Product product) {
        return product.getPhotos().stream()
                .map(photo -> storageService.getImageUrl(photo.getFileName()))
                .toList();
    }
}
