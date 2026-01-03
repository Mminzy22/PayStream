package com.paystream.inventory.product.service;

import static com.paystream.core.exception.ExceptionEnum.PRODUCT_NOT_FOUND;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

import com.paystream.core.exception.PayStreamException;
import com.paystream.inventory.inventory.entity.DailyInventory;
import com.paystream.inventory.inventory.repository.DailyInventoryRepository;
import com.paystream.inventory.product.entity.Product;
import com.paystream.inventory.product.repository.ProductRepository;
import com.paystream.inventory.store.entity.Category;
import com.paystream.inventory.store.entity.Store;
import com.paystream.inventory.store.repository.StoreRepository;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@Transactional
@ActiveProfiles("test")
@SpringBootTest
class ProductFindServiceTest {

    @Autowired private ProductRepository productRepository;

    @Autowired private StoreRepository storeRepository;

    @Autowired private DailyInventoryRepository dailyInventoryRepository;

    @Autowired private ProductFindService productFindService;

    @Autowired private EntityManager em;

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

    @DisplayName("[성공] 상품 조회시 체크인, 체크아웃에 해당하는 상품과 재고를 조회한다.")
    @Test
    void testFindProductWithDailyInventory() {
        em.flush();
        em.clear();

        // given
        Long productId = savedProduct.getId();
        LocalDate checkInDate = LocalDate.now();
        LocalDate checkOutDate = LocalDate.now().plusDays(2);

        // when
        Product findProduct =
                productRepository
                        .findById(productId)
                        .orElseThrow(() -> new PayStreamException(PRODUCT_NOT_FOUND));

        List<DailyInventory> dailyInventories =
                findProduct.getDailyInventories().stream()
                        .filter(
                                inventory -> {
                                    LocalDate inventoryDate = inventory.getDate();
                                    return !inventoryDate.isBefore(checkInDate)
                                            && inventoryDate.isBefore(checkOutDate);
                                })
                        .toList();

        // then
        assertThat(dailyInventories).hasSize(2);
    }
}
