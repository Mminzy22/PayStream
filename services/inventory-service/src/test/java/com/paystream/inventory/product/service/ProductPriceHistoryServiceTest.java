package com.paystream.inventory.product.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.paystream.inventory.inventory.entity.DailyInventory;
import com.paystream.inventory.inventory.repository.DailyInventoryRepository;
import com.paystream.inventory.product.dto.response.ProductPriceHistoryResponse;
import com.paystream.inventory.product.entity.Product;
import com.paystream.inventory.product.repository.ProductRepository;
import com.paystream.inventory.promotion.entity.DiscountType;
import com.paystream.inventory.promotion.entity.Promotion;
import com.paystream.inventory.promotion.entity.PromotionStatus;
import com.paystream.inventory.promotion.entity.TargetType;
import com.paystream.inventory.promotion.repository.PromotionRepository;
import com.paystream.inventory.store.entity.Store;
import com.paystream.inventory.store.repository.StoreRepository;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import org.assertj.core.groups.Tuple;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

@Transactional
@ActiveProfiles("test")
@SpringBootTest
class ProductPriceHistoryServiceTest {

    @Autowired private ProductPriceHistoryService productPriceHistoryService;

    @Autowired private ProductRepository productRepository;

    @Autowired private PromotionRepository promotionRepository;

    @Autowired private StoreRepository storeRepository;

    @Autowired private DailyInventoryRepository dailyInventoryRepository;

    @DisplayName("해당 숙소의 역대 세일 내역 리스트롤 조회할 수 있다,")
    @Test
    void testGetPriceHistoryDetails() {
        // given
        Store store = Store.builder().hostId("host-1").name("TEST STORE").build();
        Store savedStore = storeRepository.save(store);

        Product product =
                Product.builder()
                        .store(savedStore)
                        .name("TEST PRODUCT")
                        .basePrice(10000)
                        .baseStock(5)
                        .build();
        Product savedProduct = productRepository.save(product);

        List<DailyInventory> inventories = new ArrayList<>();
        for (int i = 0; i < 3; i++) {
            DailyInventory inventory =
                    DailyInventory.builder()
                            .product(savedProduct)
                            .date(LocalDate.now().plusDays(i))
                            .stockAvailable(5)
                            .build();

            inventories.add(inventory);
        }
        dailyInventoryRepository.saveAll(inventories);

        List<Promotion> promotions = new ArrayList<>();
        for (int i = 0; i < 3; i++) {
            Promotion promotion =
                    Promotion.builder()
                            .createUserId("host-1")
                            .title("TEST PROMOTION")
                            .targetId(savedProduct.getId())
                            .targetType(TargetType.PRODUCT)
                            .discountType(DiscountType.PERCENT)
                            .discountValue((i + 1) * 10)
                            .startDate(LocalDate.now().plusDays(i))
                            .endDate(LocalDate.now().plusDays(i + 1))
                            .status(PromotionStatus.ACTIVE)
                            .build();
            promotions.add(promotion);
        }
        promotionRepository.saveAll(promotions);

        Long productId = savedProduct.getId();
        LocalDate startDate = LocalDate.now();
        LocalDate endDate = LocalDate.now().plusDays(2);

        // when
        List<ProductPriceHistoryResponse> result =
                productPriceHistoryService.getPriceHistoryDetails(productId, startDate, endDate);

        // then
        assertThat(result)
                .hasSize(2)
                .extracting(
                        "startDate", "endDate", "originPrice", "discountedPrice", "discountRate")
                .containsExactly( // 시작일자 내림차순
                        Tuple.tuple(
                                LocalDate.now().plusDays(1),
                                LocalDate.now().plusDays(2),
                                (long) product.getBasePrice(),
                                8000L,
                                20.0),
                        Tuple.tuple(
                                LocalDate.now(),
                                LocalDate.now().plusDays(1),
                                (long) product.getBasePrice(),
                                9000L,
                                10.0));
    }
}
