package com.paystream.inventory.product.service;

import static com.paystream.core.exception.ExceptionEnum.NOT_STORE_HOST;
import static com.paystream.core.exception.ExceptionEnum.STORE_NOT_FOUND;

import com.paystream.core.exception.ExceptionEnum;
import com.paystream.core.exception.PayStreamException;
import com.paystream.inventory.inventory.entity.DailyInventory;
import com.paystream.inventory.inventory.repository.DailyInventoryRepository;
import com.paystream.inventory.product.dto.request.ProductCreateRequest;
import com.paystream.inventory.product.entity.Product;
import com.paystream.inventory.product.repository.ProductRepository;
import com.paystream.inventory.store.entity.Store;
import com.paystream.inventory.store.repository.StoreRepository;
import jakarta.transaction.Transactional;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Transactional
@Service
@RequiredArgsConstructor
public class ProductCreateService {

    private final StoreRepository storeRepository;
    private final ProductRepository productRepository;
    private final DailyInventoryRepository dailyInventoryRepository;

    public Long create(String hostId, ProductCreateRequest request) {
        // 존재하는 가게인지 확인
        Store findStore =
                storeRepository
                        .findById(request.getStoreId())
                        .orElseThrow(() -> new PayStreamException(STORE_NOT_FOUND));

        if (!findStore.getHostId().equals(hostId)) {
            throw new PayStreamException(NOT_STORE_HOST);
        }

        // 상품 존재 확인
        boolean isProductName =
                productRepository.existsByStoreIdAndName(findStore.getId(), request.getName());
        if (isProductName) {
            throw new PayStreamException(ExceptionEnum.PRODUCT_ALREADY_EXISTS);
        }

        // 상품 저장
        Product product = request.toEntity();
        product.assignStore(findStore);
        Product savedProduct = productRepository.save(product);

        // 상품의 날짜별 재고수량 채우기 (한달)
        LocalDate today = LocalDate.now();
        LocalDate until = today.plusDays(30);
        List<DailyInventory> dailyInventories =
                today.datesUntil(until)
                        .map(
                                date ->
                                        DailyInventory.builder()
                                                .product(savedProduct)
                                                .date(date)
                                                .build())
                        .toList();
        dailyInventoryRepository.saveAll(dailyInventories);

        return savedProduct.getId();
    }
}
