package com.paystream.inventory.product.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;

import com.paystream.core.exception.PayStreamException;
import com.paystream.inventory.inventory.entity.DailyInventory;
import com.paystream.inventory.inventory.repository.DailyInventoryRepository;
import com.paystream.inventory.product.dto.request.ProductDeleteRequest;
import com.paystream.inventory.product.entity.Product;
import com.paystream.inventory.product.repository.ProductRepository;
import com.paystream.inventory.store.entity.Category;
import com.paystream.inventory.store.entity.Store;
import com.paystream.inventory.store.repository.StoreRepository;
import jakarta.transaction.Transactional;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@Transactional
@ActiveProfiles("test")
@SpringBootTest
class ProductDeleteServiceTest {

    @Autowired private ProductDeleteService productDeleteService;

    @Autowired private ProductRepository productRepository;

    @Autowired private StoreRepository storeRepository;

    @Autowired private DailyInventoryRepository dailyInventoryRepository;

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
                        .minPersonCount(2)
                        .maxPersonCount(3)
                        .basePrice(10000)
                        .personAddPrice(10000)
                        .build();

        savedStore.addProduct(product);
        this.savedProduct = productRepository.save(product);

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
    }

    @DisplayName("[성공] 상품 삭제")
    @Test
    void testProductDelete() {
        // given
        ProductDeleteRequest request =
                ProductDeleteRequest.builder().storeId(savedStore.getId()).build();

        // when
        productDeleteService.delete(hostId, savedProduct.getId(), request);

        Optional<Product> deletedProduct = productRepository.findById(savedProduct.getId());

        // then
        assertThat(deletedProduct).isEmpty();
    }

    @DisplayName("[성공] 상품 삭제시 상품의 재고도 함께 삭제")
    @Test
    void testDeleteProductWithInventories() {
        // given
        ProductDeleteRequest request =
                ProductDeleteRequest.builder().storeId(savedStore.getId()).build();

        // when
        productDeleteService.delete(hostId, savedProduct.getId(), request);

        List<DailyInventory> deletedInventories =
                dailyInventoryRepository.findByProductId(savedProduct.getId());

        // then
        assertThat(deletedInventories).isEmpty();
    }

    @DisplayName("[실패] 상품이 예약되어 재고가 차감되어 있을때는 상품을 제거할 수 없다.")
    @Test
    void failDeleteProductWhenReservationsExist() {
        // given
        ProductDeleteRequest request =
                ProductDeleteRequest.builder().storeId(savedStore.getId()).build();

        // 재고 차감
        List<DailyInventory> dailyInventories =
                dailyInventoryRepository.findByProductId(savedProduct.getId());
        dailyInventories.forEach(DailyInventory::decreaseStockAvailable);

        // when
        // then
        assertThatThrownBy(() -> productDeleteService.delete(hostId, savedProduct.getId(), request))
                .isInstanceOf(PayStreamException.class)
                .hasMessageContaining("삭제가 불가능한 상품이 있습니다 다시 확인해주세요.");
    }
}
