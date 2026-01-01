package com.paystream.inventory.store.service;

import static com.paystream.core.exception.ExceptionEnum.*;

import com.paystream.core.exception.PayStreamException;
import com.paystream.inventory.inventory.entity.DailyInventory;
import com.paystream.inventory.inventory.repository.DailyInventoryRepository;
import com.paystream.inventory.product.entity.Product;
import com.paystream.inventory.store.dto.request.StoreDeleteRequest;
import com.paystream.inventory.store.entity.Store;
import com.paystream.inventory.store.repository.StoreRepository;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class StoreDeleteService {

    private final StoreRepository storeRepository;
    private final DailyInventoryRepository dailyInventoryRepository;

    @Transactional
    public void deleted(String hostId, StoreDeleteRequest request) {
        // 삭제하고자 하는 가게가 존재하는지 확인
        List<Store> existedStore = storeRepository.findByIdIn(request.getStoreIds());
        if (existedStore.isEmpty()) {
            throw new PayStreamException(STORE_NOT_FOUND);
        }

        // 조회된 가게들의 host가 요청한 host가 맞는지 확인
        boolean isHostExclusive =
                existedStore.stream().allMatch(store -> store.getHostId().equals(hostId));
        if (!isHostExclusive) {
            throw new PayStreamException(STORE_ACCESS_DENIED);
        }

        // 조회된 가게들 중 요청한 가게가 포함되어있지 않는지 확인
        if (request.getStoreIds().size() != existedStore.size()) {
            throw new PayStreamException(STORE_DELETION_BLOCKED);
        }

        List<Long> productIds = getProductIdsByStore(existedStore);

        // 현재 날짜를 기준으로 상품들의 날짜별 재고를 조회
        List<DailyInventory> futureInventories =
                dailyInventoryRepository.findByProductIdInAndDateGreaterThanEqual(
                        productIds, LocalDate.now());

        // 재고가 하나라도 깎여있으면(예약되어 있으면) 삭제 불가
        boolean hasReservation =
                futureInventories.stream()
                        .anyMatch(i -> i.getStockAvailable() < i.getProduct().getBaseStock());

        if (hasReservation) {
            throw new PayStreamException(STORE_DELETION_BLOCKED);
        }

        // 확인이 되었다면 삭제
        storeRepository.deleteAllById(request.getStoreIds());
    }

    private static List<Long> getProductIdsByStore(List<Store> existedStore) {
        return existedStore.stream()
                .flatMap(s -> s.getProducts().stream())
                .map(Product::getId)
                .distinct()
                .toList();
    }
}
