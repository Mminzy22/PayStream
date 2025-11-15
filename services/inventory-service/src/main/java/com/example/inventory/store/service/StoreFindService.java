package com.example.inventory.store.service;

import com.example.inventory.store.dto.response.StoreResponse;
import com.example.inventory.store.dto.request.StoreListFindRequest;
import com.example.inventory.inventory.entity.DailyInventory;
import com.example.inventory.product.entity.Product;
import com.example.inventory.store.entity.Store;
import com.example.inventory.inventory.repository.DailyInventoryRepository;
import com.example.inventory.store.repository.StoreQueryDslRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.*;

@Transactional(readOnly = true)
@Slf4j
@Service
@RequiredArgsConstructor
public class StoreFindService {

    private final StoreQueryDslRepository storeQueryDslRepository;
    private final DailyInventoryRepository dailyInventoryRepository;

    // 조회 성능 향상을 위한 Redis 캐싱 기능 추가하기
    public List<StoreResponse> userFindStoreList(StoreListFindRequest request, Pageable reqPageable) {
        Pageable pageable = PageRequest.of(reqPageable.getPageNumber() - 1, reqPageable.getPageSize());

        Page<Store> stores = storeQueryDslRepository.findAllByFetchJoin(request, pageable);
        if(!stores.hasContent()) {
            throw new EntityNotFoundException("Store not found");
        }

        Map<Long, Integer> storeMap = stores.stream()
                .collect(toMap(
                        Store::getId,
                        store -> store.getProducts().stream()
                                .map(Product::getBasePrice)
                                .min(Comparator.naturalOrder())
                                .orElse(0)
                ));

        // 가게별 상품의 최저 금액 계산
        return stores.stream()
                .map(store -> {
                    int minPrice = storeMap.get(store.getId());
                    return StoreResponse.of(store, minPrice);
                })
                .toList();

    }


    /**
     * 가게 상세 조회
     * @param id
     * @param checkInDate
     * @param checkOutDate
     * @return StoreResponse
     */
    public StoreResponse findStore(Long id, LocalDate checkInDate, LocalDate checkOutDate, int personCount) {
        Store store = storeQueryDslRepository.findOne(id, checkInDate, checkOutDate, personCount)
                .orElseThrow(() -> new EntityNotFoundException("Store not found"));



        return StoreResponse.ofWithProducts(store);
    }
}
