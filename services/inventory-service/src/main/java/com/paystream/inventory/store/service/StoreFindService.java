package com.paystream.inventory.store.service;

import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toList;

import com.paystream.core.exception.ExceptionEnum;
import com.paystream.core.exception.PayStreamException;
import com.paystream.inventory.annotation.CacheKeyParam;
import com.paystream.inventory.config.PageResponse;
import com.paystream.inventory.inventory.entity.DailyInventory;
import com.paystream.inventory.inventory.repository.DailyInventoryRepository;
import com.paystream.inventory.product.dto.response.ProductResponse;
import com.paystream.inventory.product.entity.Product;
import com.paystream.inventory.product.repository.ProductRepository;
import com.paystream.inventory.store.dto.request.StoreFindRequest;
import com.paystream.inventory.store.dto.request.StoreListFindRequest;
import com.paystream.inventory.store.dto.response.StoreResponse;
import com.paystream.inventory.store.entity.Store;
import com.paystream.inventory.store.repository.StoreQueryDslRepository;
import jakarta.persistence.EntityNotFoundException;
import java.util.Collections;
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

    private final ProductRepository productRepository;
    private final StoreQueryDslRepository storeQueryDslRepository;
    private final DailyInventoryRepository dailyInventoryRepository;

    // 조회 성능 향상을 위한 Redis 캐싱 기능 추가하기
    @Cacheable(value = "stores", keyGenerator = "customKeyGenerator")
    public PageResponse<StoreResponse> userFindStoreList(
            @CacheKeyParam StoreListFindRequest request, Pageable reqPageable) {
        Pageable pageable =
                PageRequest.of(reqPageable.getPageNumber() - 1, reqPageable.getPageSize());

        // 가게 전체 조회
        Page<Store> stores = storeQueryDslRepository.findAllByFetchJoin(request, pageable);

        if (stores.isEmpty()) {
            // 빈값이면 비어 있는 상태로 반환
            Page<StoreResponse> emptyResponse = stores.map(StoreResponse::of);
            return new PageResponse<>(emptyResponse);
        }

        // 기간에 해당하는 가게의 상품들을 조회
        List<Long> storeIds = stores.getContent().stream().map(Store::getId).toList();
        Map<Long, List<Product>> productsByStoreId =
                productRepository
                        .findAvailableProducts(
                                storeIds, request.getCheckInDate(), request.getCheckOutDate())
                        .stream()
                        .collect(groupingBy(p -> p.getStore().getId()));

        // 가게별 상품의 최저 금액 계산
        Page<StoreResponse> responsePage =
                stores.map(
                        store -> {
                            List<Product> storeProducts =
                                    productsByStoreId.getOrDefault(
                                            store.getId(), Collections.emptyList());

                            int minPrice =
                                    storeProducts.stream()
                                            .mapToInt(Product::getBasePrice)
                                            .min()
                                            .orElse(0);

                            return StoreResponse.of(store, minPrice);
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
        Store store = null;

        try {
            store =
                    storeQueryDslRepository
                            .findOne(id, request.getCheckInDate(), request.getCheckOutDate())
                            .orElseThrow(EntityNotFoundException::new);
        } catch (EntityNotFoundException e) {
            throw new PayStreamException(ExceptionEnum.STORE_NOT_FOUND);
        } catch (Exception e) {
            throw new PayStreamException(ExceptionEnum.INTERNAL_SERVER_ERROR);
        }

        List<Product> products = store.getProducts();
        List<Long> productIds = products.stream().map(Product::getId).toList();

        Map<Long, List<DailyInventory>> inventoriesMap =
                dailyInventoryRepository.findByProductIdIn(productIds).stream()
                        .collect(groupingBy(d -> d.getProduct().getId(), toList()));

        List<ProductResponse> productResponses =
                products.stream()
                        .map(
                                p -> {
                                    List<DailyInventory> findInventory =
                                            inventoriesMap.getOrDefault(
                                                    p.getId(), Collections.emptyList());

                                    boolean isStock =
                                            findInventory.stream()
                                                    .allMatch(inv -> inv.getStockAvailable() > 0);

                                    // 최대 수용인원과 재고가 없을 경우 체크 (예약 가능 상품이면 true)
                                    boolean isAvailable =
                                            isStock
                                                    && request.getPersonCount()
                                                            <= p.getMaxPersonCount();

                                    return ProductResponse.of(p, isAvailable);
                                })
                        .toList();

        return StoreResponse.ofWithProducts(store, productResponses);
    }
}
