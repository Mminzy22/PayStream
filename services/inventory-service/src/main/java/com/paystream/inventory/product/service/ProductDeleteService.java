package com.paystream.inventory.product.service;

import static com.paystream.core.exception.ExceptionEnum.*;

import com.paystream.core.exception.ExceptionEnum;
import com.paystream.core.exception.PayStreamException;
import com.paystream.inventory.inventory.entity.DailyInventory;
import com.paystream.inventory.inventory.repository.DailyInventoryRepository;
import com.paystream.inventory.product.dto.request.ProductDeleteRequest;
import com.paystream.inventory.product.entity.Product;
import com.paystream.inventory.product.repository.ProductRepository;
import com.paystream.inventory.store.entity.Store;
import com.paystream.inventory.store.repository.StoreRepository;
import jakarta.transaction.Transactional;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Transactional
@Service
@RequiredArgsConstructor
public class ProductDeleteService {

    private final StoreRepository storeRepository;
    private final ProductRepository productRepository;
    private final DailyInventoryRepository dailyInventoryRepository;

    public void delete(String hostId, Long productId, ProductDeleteRequest request) {
        // 가게 주인이 맞는지 확인
        Store findStore = getStore(hostId, request);

        Product findProduct = getProduct(productId);

        containsProduct(findStore, findProduct);

        // 삭제해도 되는 상품인지 확인 (예약이 잡혀있는 상품인지)
        List<DailyInventory> deletedInventories = getDeletableInventories(findProduct);

        dailyInventoryRepository.deleteAllInBatch(deletedInventories);
        productRepository.deleteById(findProduct.getId());
    }

    private List<DailyInventory> getDeletableInventories(Product findProduct) {
        List<DailyInventory> deletedInventories =
                dailyInventoryRepository.findByProductId(findProduct.getId());
        List<DailyInventory> nowAfterInventories =
                deletedInventories.stream()
                        .filter(inventory -> !inventory.getDate().isBefore(LocalDate.now()))
                        .toList();

        boolean isInventory =
                nowAfterInventories.stream()
                        .allMatch(
                                inventory ->
                                        inventory.getStockAvailable()
                                                == findProduct.getBaseStock());
        if (!isInventory) {
            throw new PayStreamException(ExceptionEnum.PRODUCT_DELETION_BLOCKED);
        }
        return deletedInventories;
    }

    private static void containsProduct(Store findStore, Product findProduct) {
        List<Product> products = findStore.getProducts().stream().distinct().toList();
        boolean containsProduct = products.contains(findProduct);

        if (!containsProduct) {
            throw new PayStreamException(PRODUCT_STORE_MISMATCH);
        }
    }

    private Product getProduct(Long productId) {
        Product findProduct =
                productRepository
                        .findById(productId)
                        .orElseThrow(() -> new PayStreamException(ExceptionEnum.PRODUCT_NOT_FOUND));
        return findProduct;
    }

    private Store getStore(String hostId, ProductDeleteRequest request) {
        Store findStore =
                storeRepository
                        .findById(request.getStoreId())
                        .orElseThrow(() -> new PayStreamException(STORE_NOT_FOUND));

        if (!findStore.getHostId().equals(hostId)) {
            throw new PayStreamException(NOT_STORE_HOST);
        }
        return findStore;
    }
}
