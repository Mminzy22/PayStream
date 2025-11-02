package com.example.inventory.store.service;

import com.example.inventory.store.dto.response.StoreResponse;
import com.example.inventory.store.dto.request.StoreUserFindRequest;
import com.example.inventory.inventory.entity.DailyInventory;
import com.example.inventory.product.entity.Product;
import com.example.inventory.store.entity.Store;
import com.example.inventory.inventory.repository.DailyInventoryRepository;
import com.example.inventory.store.repository.StoreQueryDslRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;

import static java.util.stream.Collectors.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class StoreFindService {

    private final StoreQueryDslRepository storeQueryDslRepository;
    private final DailyInventoryRepository dailyInventoryRepository;

    // 조회 성능 향상을 위한 Redis 캐싱 기능 추가하기
    @Transactional(readOnly = true)
    public List<StoreResponse> userFindStoreList(StoreUserFindRequest request, Pageable reqPageable) {
        Pageable pageable = PageRequest.of(reqPageable.getPageNumber() - 1, reqPageable.getPageSize());

        Page<Store> stores = storeQueryDslRepository.findAllByFetchJoin(request, pageable);
        if(!stores.hasContent()) {
            throw new EntityNotFoundException("Store not found");
        }

        // 가게의 productIds를 추출
        List<Long> productIds = stores.stream()
                .flatMap(store -> store.getProducts().stream()
                        .map(Product::getId))
                .toList();

        // 상품들의 일자별 재고를 조회
        List<DailyInventory> dailyInventories = dailyInventoryRepository.findByProductIdInAndDateBetween(productIds, request.getCheckIn(), request.getCheckOut());

        // 가게별 최소 상품 금액 계산
        Map<Long, Optional<Integer>> storeMap = storesToMap(stores.toList(), dailyInventories);

        // 가게별 상품의 최저 금액 계산
        return stores.stream()
                .filter(store -> storeMap.get(store.getId()).orElse(0) > 0)
                .map(store -> {
                    int minPrice = storeMap.get(store.getId()).orElse(0);
                    return StoreResponse.of(store, minPrice);
                })
                .toList();

    }

    private Map<Long, Optional<Integer>> storesToMap(List<Store> stores, List<DailyInventory> dailyInventories) {
        Predicate<Product> isProductAvailableForPeriod = getAvailableForPeriod(dailyInventories);

        return stores.stream()
                .collect(groupingBy(
                        Store::getId, // 키: Store ID (s -> s.getId())
                        flatMapping( // Store 리스트를 Product 스트림으로 펼치기 (중복 처리)
                                s -> s.getProducts().stream()
                                        .filter(isProductAvailableForPeriod), // Product 스트림에서 basePrice의 최솟값을 찾기
                                mapping(
                                        Product::getBasePrice, // Product 객체에서 basePrice (Integer) 추출
                                        minBy(Comparator.naturalOrder()) // 추출된 값들 중 가장 작은 값 찾기
                                )
                        )
                ));
    }

    private static Predicate<Product> getAvailableForPeriod(List<DailyInventory> dailyInventories) {
        return product -> {
            Map<Long, List<Integer>> dailyInventoryToMap = dailyInventories.stream()
                    .filter(dailyInventory -> dailyInventory.getProduct().getId().equals(product.getId()))
                    .collect(groupingBy(
                            dailyInventory -> dailyInventory.getProduct().getId(),
                            mapping(
                                    DailyInventory::getStockAvailable,
                                    toList()
                            )
                    ));

            // 필수 날짜 목록을 순회하면서 하나라도 재고가 없거나, 인벤토리 기록이 없으면 false를 반환
            List<Integer> stocks = dailyInventoryToMap.get(product.getId());
            if(stocks == null || stocks.isEmpty()) {
                return false;
            }

            // 인벤토리 기록이 존재하고, Stock != NULL, 재고가 0보다 커야함.
            return stocks.stream()
                    .allMatch(stock -> stock != null && stock > 0);
        };
    }

}
