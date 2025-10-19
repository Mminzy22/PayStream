package com.example.inventory.service;

import com.example.inventory.dto.store.StoreResponse;
import com.example.inventory.dto.store.request.StoreUserFindRequest;
import com.example.inventory.entity.inventory.DailyInventory;
import com.example.inventory.entity.product.Product;
import com.example.inventory.entity.store.Store;
import com.example.inventory.repository.DailyInventoryRepository;
import com.example.inventory.repository.StoreQueryDslRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;

import static java.util.stream.Collectors.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class StoreService {

    private final StoreQueryDslRepository storeQueryDslRepository;
    private final DailyInventoryRepository dailyInventoryRepository;

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
        Predicate<Product> isProductAvailableForPeriod = getAvailableForPeriod2(dailyInventories);

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

    private static Predicate<Product> getAvailableForPeriod2(List<DailyInventory> dailyInventories) {
        return product -> {
            Map<Long, List<Integer>> dailyInventoryToMap = dailyInventories.stream()
                    .collect(groupingBy(
                            dailyInventory -> dailyInventory.getProduct().getId(),
                            mapping(
                                    DailyInventory::getStockAvailable,
                                    toList()
                            )
                    ));

            // 필수 날짜 목록을 순회하면서 하나라도 재고가 없거나, 인벤토리 기록이 없으면 false를 반환
            return dailyInventoryToMap.get(product.getId()).stream()
                    .allMatch(stock -> stock != null && stock > 0); // 인벤토리 기록이 존재하고, Stock != NULL, 재고가 0보다 커야함.
        };
    }

    private Map<Long, Optional<Integer>> storesToMap(List<Store> stores, LocalDate checkIn, LocalDate checkOut) {
        // 필요한 모든 날짜 목록을 생성 (checkIn 포함, checkOut 미포함)
        List<LocalDate> requiredDates = checkIn.datesUntil(checkOut).toList();

        if(requiredDates.isEmpty()) {
            throw new IllegalArgumentException("Required dates are empty");
        }

        Predicate<Product> isProductAvailableForPeriod = getAvailableForPeriod(requiredDates);

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

    private static Predicate<Product> getAvailableForPeriod(List<LocalDate> requiredDates) {
        return product -> {
            Map<LocalDate, Integer> inventoryMap = product.getDailyInventories().stream()
                    .collect(toMap(
                            DailyInventory::getDate,
                            DailyInventory::getStockAvailable
                    ));


            // 필수 날짜 목록을 순회하면서 하나라도 재고가 없거나, 인벤토리 기록이 없으면 false를 반환
            return requiredDates.stream()
                    .allMatch(date -> {
                        Integer stock = inventoryMap.get(date);

                        // 인벤토리 기록이 존재하고, Stock != NULL, 재고가 0보다 커야함.
                        return stock != null && stock > 0;
                    });
        };
    }

}
