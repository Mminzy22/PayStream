package com.paystream.inventory.product.service;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;

import com.paystream.core.exception.PayStreamException;
import com.paystream.inventory.product.dto.request.ProductUpdateRequest;
import com.paystream.inventory.product.entity.Product;
import com.paystream.inventory.product.repository.ProductRepository;
import com.paystream.inventory.store.entity.Category;
import com.paystream.inventory.store.entity.Store;
import com.paystream.inventory.store.repository.StoreRepository;
import jakarta.transaction.Transactional;
import java.time.LocalTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles("test")
@Transactional
@SpringBootTest
class ProductUpdateServiceTest {

    @Autowired private ProductUpdateService productUpdateService;

    @Autowired private ProductRepository productRepository;

    @Autowired private StoreRepository storeRepository;

    private Store savedStore;
    private Product savedProduct;
    private String hostId = "1";

    @BeforeEach
    void setUp() {
        Store store =
                Store.builder()
                        .name("Test Store")
                        .description("Test Store description")
                        .hostId("1")
                        .checkInTime(LocalTime.now())
                        .checkOutTime(LocalTime.now().plusHours(1))
                        .category(Category.HOTEL)
                        .build();

        this.savedStore = storeRepository.save(store);

        Product product =
                Product.builder()
                        .store(savedStore)
                        .name("Test Product")
                        .description("Test Product description")
                        .minCapacity(2)
                        .maxCapacity(3)
                        .basePrice(10000)
                        .personAddPrice(10000)
                        .build();

        savedStore.addProduct(product);
        this.savedProduct = productRepository.save(product);
    }

    @DisplayName("[성공] 상품 업데이트")
    @Test
    void testUpdateProduct() {
        // given
        ProductUpdateRequest request =
                ProductUpdateRequest.builder()
                        .storeId(savedStore.getId())
                        .name("Modified Test Product")
                        .description("Modified Test Product description")
                        .basePrice(20000)
                        .personAddPrice(2000)
                        .minCapacity(2)
                        .maxCapacity(4)
                        .build();
        // when
        Long updatedProductId = productUpdateService.update(hostId, savedProduct.getId(), request);
        Product updatedProduct = productRepository.findById(updatedProductId).orElse(null);

        // then
        assertThat(updatedProduct)
                .isNotNull()
                .extracting(
                        "name",
                        "description",
                        "basePrice",
                        "personAddPrice",
                        "minCapacity",
                        "maxCapacity")
                .containsExactly(
                        request.getName(),
                        request.getDescription(),
                        request.getBasePrice(),
                        request.getPersonAddPrice(),
                        request.getMinCapacity(),
                        request.getMaxCapacity());
    }

    @DisplayName("[실패] 업데이트할 상품이 가게에 없으면 예외 발생")
    @Test
    void testFindStoreWithoutProduct() {
        // given

        // 다른 가게와 상품 등록
        Store store =
                Store.builder()
                        .name("Test Store")
                        .description("Test Store description")
                        .hostId("1")
                        .checkInTime(LocalTime.now())
                        .checkOutTime(LocalTime.now().plusHours(1))
                        .category(Category.HOTEL)
                        .build();

        Store otherStore = storeRepository.save(store);

        Product product =
                Product.builder()
                        .store(otherStore)
                        .name("Test Product")
                        .description("Test Product description")
                        .minCapacity(2)
                        .maxCapacity(3)
                        .basePrice(10000)
                        .personAddPrice(10000)
                        .build();

        otherStore.addProduct(product);
        Product otherProduct = productRepository.save(product);

        // 수정하고자 하는 상품
        ProductUpdateRequest request =
                ProductUpdateRequest.builder()
                        .storeId(savedStore.getId()) // 수정하고자 하는 상품의 가게와 다름.
                        .name("Modified Test Product")
                        .description("Modified Test Product description")
                        .basePrice(20000)
                        .personAddPrice(2000)
                        .minCapacity(2)
                        .maxCapacity(4)
                        .build();

        Long storeWithoutProductId = otherProduct.getId();

        // when
        // then
        assertThatThrownBy(
                        () -> productUpdateService.update(hostId, storeWithoutProductId, request))
                .isInstanceOf(PayStreamException.class)
                .hasMessage("가게에 포함된 상품이 아닙니다.");
    }
}
