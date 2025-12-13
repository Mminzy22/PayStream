package com.paystream.inventory.product.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

import com.paystream.inventory.inventory.repository.DailyInventoryRepository;
import com.paystream.inventory.product.dto.request.ProductCreateRequest;
import com.paystream.inventory.product.repository.ProductRepository;
import com.paystream.inventory.store.entity.Category;
import com.paystream.inventory.store.entity.Store;
import com.paystream.inventory.store.repository.StoreRepository;
import java.time.LocalTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles("test")
@SpringBootTest
class ProductCreateServiceTest {

    @Autowired private ProductCreateService productCreateService;

    @Autowired private StoreRepository storeRepository;

    @Autowired private ProductRepository productRepository;

    @Autowired private DailyInventoryRepository dailyInventoryRepository;

    String hostId = "1";

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
        storeRepository.save(store);
    }

    @DisplayName("[성공] 상품 생성")
    @Test
    void testCreateProduct() {
        // given
        ProductCreateRequest request =
                ProductCreateRequest.builder()
                        .storeId(1L)
                        .name("Test Product")
                        .description("Test Product description")
                        .basePrice(10000)
                        .personAddPrice(10000)
                        .minCapacity(2)
                        .maxCapacity(3)
                        .stock(5)
                        .build();

        // when
        Long savedProductId = productCreateService.create(hostId, request);

        // then
        assertThat(savedProductId).isEqualTo(1L);
    }

    @DisplayName("[실패] 상품 생성 시 가게 주인이 다르면 예외가 발생한다")
    @Test
    void testCreateProductThrowNotStoreHost() {
        // given

        // when

        // then
    }

    @DisplayName("[실패] 상품 생성 시 가게 내에서 상품명이 겹쳐서 예외가 발생한다")
    @Test
    void testCreateProductThrowDuplicateProductName() {
        // given

        // when

        // then
    }

    @DisplayName("[성공] 상품 생성 시 다른 가게의 상품명과 겹쳐도 예외가 발생하지 않는다")
    @Test
    void testCreateProductWithName() {
        // given

        // when

        // then
    }

    @DisplayName("[성공] 상품 생성 시 상품에 대한 재고가 생성된다.")
    @Test
    void testCreateProductWithCreateDailyInventory() {
        // given

        // when

        // then
    }
}
