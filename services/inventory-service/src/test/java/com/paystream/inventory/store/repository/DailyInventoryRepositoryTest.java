package com.paystream.inventory.store.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

import com.paystream.inventory.inventory.entity.DailyInventory;
import com.paystream.inventory.inventory.repository.DailyInventoryRepository;
import com.paystream.inventory.product.entity.Product;
import com.paystream.inventory.store.entity.Address;
import com.paystream.inventory.store.entity.Category;
import com.paystream.inventory.store.entity.Store;
import jakarta.persistence.EntityManager;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles("test")
@DataJpaTest
class DailyInventoryRepositoryTest {

    @Autowired private DailyInventoryRepository dailyInventoryRepository;

    @Autowired private EntityManager entityManager; // 테스트 클래스 상단에 추가

    @DisplayName("상품Id들과 체크인, 체크아웃으로 해당 상품들의 재고를 조회한다.")
    @Test
    void findByProductIdInAndDateBefore() {
        // given
        Store mockStore = createMockStore(99L);

        Product product1 = createMockProduct(1L, mockStore);
        Product product2 = createMockProduct(2L, mockStore);
        Product product3 = createMockProduct(3L, mockStore);

        List<Long> productIds = List.of(product1.getId(), product2.getId(), product3.getId());

        // 재고 데이터 생성
        DailyInventory inventory1 =
                DailyInventory.builder()
                        .product(product1)
                        .date(LocalDate.now().plusDays(1))
                        .stockAvailable(10)
                        .build();
        DailyInventory inventory2 =
                DailyInventory.builder()
                        .product(product2)
                        .date(LocalDate.now().plusDays(2))
                        .stockAvailable(0)
                        .build();
        DailyInventory inventory3 =
                DailyInventory.builder()
                        .product(product3)
                        .date(LocalDate.now().plusDays(4))
                        .stockAvailable(5)
                        .build();

        // DailyInventory 저장 (실제 DB에 반영)
        dailyInventoryRepository.saveAll(List.of(inventory1, inventory2));

        // 트랜잭션 마무리 (데이터가 확실히 DB에 반영되도록)
        entityManager.flush();
        entityManager.clear();

        // when
        LocalDate checkIn = LocalDate.now().plusDays(1);
        LocalDate checkOut = LocalDate.now().plusDays(3);

        List<DailyInventory> result =
                dailyInventoryRepository.findByProductIdInAndDateBetween(
                        productIds, checkIn, checkOut);

        // then
        assertThat(result)
                .hasSize(2)
                .extracting("product.name", "stockAvailable")
                .containsExactlyInAnyOrder(tuple("MockProduct1", 10), tuple("MockProduct2", 0));
        assertThat(result)
                .extracting("product.store.name")
                .containsExactly("MockStore99", "MockStore99");
    }

    @DisplayName("조회된 일자별 재고를 상품ID로 재 조회")
    @Test
    void findDailyInventoryByProductId() {
        // given
        Store mockStore = createMockStore(99L);

        Product product1 = createMockProduct(1L, mockStore);
        Product product2 = createMockProduct(2L, mockStore);
        Product product3 = createMockProduct(3L, mockStore);

        List<Long> productIds = List.of(product1.getId(), product2.getId(), product3.getId());

        // 재고 데이터 생성
        DailyInventory inventory1 =
                DailyInventory.builder()
                        .product(product1)
                        .date(LocalDate.now().plusDays(1))
                        .stockAvailable(10)
                        .build();
        DailyInventory inventory2 =
                DailyInventory.builder()
                        .product(product2)
                        .date(LocalDate.now().plusDays(2))
                        .stockAvailable(0)
                        .build();
        DailyInventory inventory3 =
                DailyInventory.builder()
                        .product(product3)
                        .date(LocalDate.now().plusDays(4))
                        .stockAvailable(5)
                        .build();

        // DailyInventory 저장 (실제 DB에 반영)
        dailyInventoryRepository.saveAll(List.of(inventory1, inventory2));

        // 트랜잭션 마무리 (데이터가 확실히 DB에 반영되도록)
        entityManager.flush();
        entityManager.clear();

        // when
        LocalDate checkIn = LocalDate.now().plusDays(1);
        LocalDate checkOut = LocalDate.now().plusDays(3);

        List<DailyInventory> result =
                dailyInventoryRepository.findByProductIdInAndDateBetween(
                        productIds, checkIn, checkOut);

        // then
        assertThat(result)
                .hasSize(2)
                .extracting("product.name", "stockAvailable")
                .containsExactlyInAnyOrder(tuple("MockProduct1", 10), tuple("MockProduct2", 0));
        assertThat(result)
                .extracting("product.store.name")
                .containsExactly("MockStore99", "MockStore99");
    }

    private Store createMockStore(Long storeId) {
        Store store =
                Store.builder()
                        .hostId("mockHost")
                        .name("MockStore" + storeId)
                        .checkInTime(LocalTime.of(15, 0))
                        .checkOutTime(LocalTime.of(11, 0))
                        .address(new Address("MockCity", "MockStreet"))
                        .category(Category.HOTEL) // 필수 Enum 값
                        .amenities(new ArrayList<>())
                        .build();

        entityManager.persist(store);
        return store;
    }

    private Product createMockProduct(Long productId, Store store) {
        Product product =
                Product.builder()
                        .name("MockProduct" + productId)
                        .basePrice(100)
                        .store(store)
                        .build();

        entityManager.persist(product);
        return product;
    }
}
