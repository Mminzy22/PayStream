package com.paystream.inventory.scheduler;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

import com.paystream.inventory.inventory.entity.DailyInventory;
import com.paystream.inventory.inventory.repository.DailyInventoryRepository;
import com.paystream.inventory.product.entity.Product;
import com.paystream.inventory.store.entity.Address;
import com.paystream.inventory.store.entity.Category;
import com.paystream.inventory.store.entity.Store;
import com.paystream.inventory.store.repository.StoreRepository;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

@ActiveProfiles("test")
@Transactional
@SpringBootTest
class InventoryProvisioningSchedulerTest {

    @Autowired private StoreRepository storeRepository;

    @Autowired private DailyInventoryRepository dailyInventoryRepository;

    @Autowired private InventoryProvisioningScheduler scheduler;

    private List<Product> products = new ArrayList<>(); // 여러 상품을 담을 리스트

    @BeforeEach
    void setUp() {
        // 1. 기존 데이터 초기화 (전체 테스트 시 영향 방지)
        storeRepository.deleteAllInBatch();

        // 2. 가게 생성
        Store store =
                Store.builder()
                        .hostId("host-1")
                        .name("테스트 가게")
                        .address(new Address("테스트 지역", "테스트 도시"))
                        .category(Category.HOTEL)
                        .checkInTime(LocalTime.now())
                        .checkOutTime(LocalTime.now())
                        .basePersonCount(2)
                        .build();

        // 3. 여러 개의 상품 생성 (예: 3개)
        for (int i = 1; i <= 3; i++) {
            Product product =
                    Product.builder()
                            .name("테스트 상품 " + i)
                            .description("테스트 설명 " + i)
                            .minPersonCount(2)
                            .maxPersonCount(2)
                            .basePrice(1000 * i)
                            .personAddPrice(1000)
                            .baseStock(5)
                            .build();

            // 오늘 날짜 재고만 우선 하나씩 생성 (스케줄러 테스트용)
            DailyInventory inventory =
                    DailyInventory.builder().date(LocalDate.now()).stockAvailable(5).build();

            product.addDailyInventory(inventory);
            store.addProduct(product);

            // 리스트에 보관하여 나중에 개별 접근 가능하게 함
            this.products.add(product);
        }

        // 4. 저장 (Cascade 설정으로 상품과 초기 재고가 함께 저장됨)
        storeRepository.save(store);
    }

    @DisplayName("[성공] 상품의 날짜별 재고가 오늘것밖에 없다면 오늘로부터 30일치가 생성된다.")
    @Test
    void ensureThirtyDaysInventory_StartingFrom_Today() {
        // given

        // when
        scheduler.scheduled();

        // then
        List<DailyInventory> dailyInventories =
                dailyInventoryRepository.findByProductId(products.get(0).getId());
        assertThat(dailyInventories).hasSize(30);

        // 오늘 포함 30일이기 때문에 plusDays는 29를 더하는 것.
        assertThat(dailyInventories.get(dailyInventories.size() - 1).getDate())
                .isEqualTo(LocalDate.now().plusDays(29));
    }
}
