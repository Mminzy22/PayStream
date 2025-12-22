package com.paystream.inventory.product.service;

import static com.paystream.core.exception.ExceptionEnum.PRODUCT_NOT_FOUND;

import com.paystream.core.exception.PayStreamException;
import com.paystream.inventory.product.dto.response.ProductDetailResponse;
import com.paystream.inventory.product.entity.Product;
import com.paystream.inventory.product.repository.ProductRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Transactional
@Service
@RequiredArgsConstructor
public class ProductFindService {

    private final ProductRepository productRepository;

    public ProductDetailResponse findProductDetail(Long productId) {
        Product findProduct =
                productRepository
                        .findById(productId)
                        .orElseThrow(() -> new PayStreamException(PRODUCT_NOT_FOUND));

        return ProductDetailResponse.of(findProduct);
    }
}
