package com.paystream.inventory.product.service;

import static com.paystream.core.exception.ExceptionEnum.*;

import com.paystream.core.exception.PayStreamException;
import com.paystream.inventory.product.dto.request.ProductUpdateRequest;
import com.paystream.inventory.product.entity.Product;
import com.paystream.inventory.product.repository.ProductRepository;
import com.paystream.inventory.store.entity.Store;
import com.paystream.inventory.store.repository.StoreRepository;
import jakarta.transaction.Transactional;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Transactional
@Service
@RequiredArgsConstructor
public class ProductUpdateService {

    private final StoreRepository storeRepository;
    private final ProductRepository productRepository;

    public Long update(String hostId, Long productId, ProductUpdateRequest request) {
        Store findStore = getStore(hostId, request);

        Product findProduct = existedProduct(productId);

        verifyProductInStore(findStore, findProduct);

        findProduct.updateInfo(request.toEntity());

        return findProduct.getId();
    }

    private void verifyProductInStore(Store findStore, Product findProduct) {
        List<Product> products = findStore.getProducts().stream().distinct().toList();
        boolean containsProduct = products.contains(findProduct);

        if (!containsProduct) {
            throw new PayStreamException(PRODUCT_STORE_MISMATCH);
        }
    }

    private Product existedProduct(Long productId) {
        return productRepository
                .findById(productId)
                .orElseThrow(() -> new PayStreamException(PRODUCT_NOT_FOUND));
    }

    private Store getStore(String hostId, ProductUpdateRequest request) {
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
