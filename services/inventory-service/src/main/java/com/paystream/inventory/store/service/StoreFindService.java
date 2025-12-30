package com.paystream.inventory.store.service;

import static java.util.stream.Collectors.*;

import com.paystream.core.exception.ExceptionEnum;
import com.paystream.core.exception.PayStreamException;
import com.paystream.inventory.annotation.CacheKeyParam;
import com.paystream.inventory.config.PageResponse;
import com.paystream.inventory.inventory.repository.DailyInventoryRepository;
import com.paystream.inventory.product.entity.Product;
import com.paystream.inventory.store.dto.request.StoreFindRequest;
import com.paystream.inventory.store.dto.request.StoreListFindRequest;
import com.paystream.inventory.store.dto.response.StoreResponse;
import com.paystream.inventory.store.entity.Store;
import com.paystream.inventory.store.repository.StoreQueryDslRepository;
import jakarta.persistence.EntityNotFoundException;
import java.util.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Transactional(readOnly = true)
@Slf4j
@Service
@RequiredArgsConstructor
public class StoreFindService {

    private final StoreQueryDslRepository storeQueryDslRepository;
    private final DailyInventoryRepository dailyInventoryRepository;

    // 조회 성능 향상을 위한 Redis 캐싱 기능 추가하기
    @Cacheable(value = "stores", keyGenerator = "customKeyGenerator")
    public PageResponse<StoreResponse> userFindStoreList(
            @CacheKeyParam StoreListFindRequest request, Pageable reqPageable) {
        Pageable pageable =
                PageRequest.of(reqPageable.getPageNumber() - 1, reqPageable.getPageSize());

        Page<Store> stores = null;

        try {
            stores = storeQueryDslRepository.findAllByFetchJoin(request, pageable);
            if (!stores.hasContent()) {
                throw new EntityNotFoundException("Store not found");
            }
        } catch (EntityNotFoundException e) {
            throw new PayStreamException(ExceptionEnum.STORE_NOT_FOUND);
        } catch (Exception e) {
            throw new PayStreamException(ExceptionEnum.INTERNAL_SERVER_ERROR);
        }

        Map<Long, Integer> storeMap =
                stores.stream()
                        .collect(
                                toMap(
                                        Store::getId,
                                        store ->
                                                store.getProducts().stream()
                                                        .map(Product::getBasePrice)
                                                        .min(Comparator.naturalOrder())
                                                        .orElse(0)));

        // 가게별 상품의 최저 금액 계산
        List<StoreResponse> responseList =
                stores.stream()
                        .map(
                                store -> {
                                    int minPrice = storeMap.get(store.getId());
                                    return StoreResponse.of(store, minPrice);
                                })
                        .toList();

        Page<StoreResponse> response =
                new PageImpl<>(responseList, stores.getPageable(), stores.getTotalElements());
        return new PageResponse<>(response);
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
                            .findOne(
                                    id,
                                    request.getCheckInDate(),
                                    request.getCheckOutDate(),
                                    request.getPersonCount())
                            .orElseThrow(() -> new EntityNotFoundException("Store not found"));
        } catch (EntityNotFoundException e) {
            throw new PayStreamException(ExceptionEnum.STORE_NOT_FOUND);
        } catch (Exception e) {
            throw new PayStreamException(ExceptionEnum.INTERNAL_SERVER_ERROR);
        }

        return StoreResponse.ofWithProducts(store);
    }
}
