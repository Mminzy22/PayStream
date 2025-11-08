package com.example.inventory.store.service;

import com.example.inventory.inventory.entity.DailyInventory;
import com.example.inventory.product.entity.Product;
import com.example.inventory.store.dto.request.StoreDeleteRequest;
import com.example.inventory.store.entity.Amenities;
import com.example.inventory.store.entity.Category;
import com.example.inventory.store.entity.Store;
import com.example.inventory.store.repository.StoreRepository;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import static com.example.inventory.store.entity.Amenities.BREAKFAST_INCLUDED;
import static com.example.inventory.store.entity.Amenities.PARKING;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@ActiveProfiles("test")
@SpringBootTest
class StoreDeleteServiceTest {

    @Autowired
    private StoreRepository storeRepository;
    @Autowired
    private StoreDeleteService storeDeleteService;

    @DisplayName("가게 삭제시 삭제해야할 가게가 없다면 오류 발생")
    @Test
    void storeDeleteByIdThrowException() {
        // given
        Store foundStore = createStore(
                "1", "한강 뷰 맛집", List.of(PARKING, BREAKFAST_INCLUDED), Category.HOTEL);

        Store savedStore = storeRepository.save(foundStore);

        StoreDeleteRequest request = StoreDeleteRequest.builder()
                .hostId("1")
                .storeIds(List.of(savedStore.getId(), 2L))
                .build();

        // when
        // then
        assertThatThrownBy(() -> storeDeleteService.deleted(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("삭제가 불가능한 가게가 있습니다 다시 확인해주세요.");
    }

    @Transactional
    @DisplayName("가게를 삭제시 같이 있던 상품들도 같이 삭제된다.")
    @Test
    void deleteStoreWithProduct() {
        // given
        List<Store> storeList = createTemplate();
        List<Long> deleteStoreIds = storeList.stream().map(Store::getId).limit(storeList.size() - 1).toList();
        StoreDeleteRequest request = StoreDeleteRequest.builder()
                .hostId("1")
                .storeIds(deleteStoreIds)
                .build();

        // when
        storeDeleteService.deleted(request);

        // then
        assertThat(storeRepository.findAll())
                .extracting(Store::getId)
                .containsExactlyInAnyOrder(storeList.get(2).getId());
    }

    private Store createStore(String hostId, String name, List<Amenities> amenities, Category category) {
        return Store.builder()
                .hostId(hostId)
                .name(name)
                .category(category)
                .amenities(amenities)
                .checkInTime(LocalTime.of(15, 0))
                .checkOutTime(LocalTime.of(11, 0))
                .build();
    }

    private Product createProduct(String name, int price) {
        return Product.builder()
                .name(name)
                .basePrice(price)
                .build();
    }

    private List<Store> createTemplate() {
        Store store1 = createStore("1", "testStore1", List.of(Amenities.PARKING, Amenities.BAR_LOUNGE), Category.HOTEL);
        Store store2 = createStore("1", "testStore2", List.of(Amenities.PARKING, Amenities.RESTAURANT), Category.PENSION);
        Store store3 = createStore("1", "testStore3", List.of(Amenities.BAR_LOUNGE, Amenities.BREAKFAST_INCLUDED), Category.GLAMPING);

        // Product 생성
        Product product1 = createProduct("product1", 1000);
        Product product2 = createProduct("product2", 2000);
        Product product3 = createProduct("product3", 3000);
        Product product4 = createProduct("product4", 4000);
        Product product5 = createProduct("product5", 5000);
        Product product6 = createProduct("product6", 6000);

        // 오늘 날짜 설정
        LocalDate today = LocalDate.now();

        // DailyInventory 추가
        // store1 (HOTEL)
        product1.addDailyInventory(DailyInventory.builder()
                .date(today)
                .stockAvailable(1) // 재고 1개
                .build());
        product2.addDailyInventory(DailyInventory.builder()
                .date(today)
                .stockAvailable(2) // 재고 2개
                .build());

        // store2 (PENSION)
        product3.addDailyInventory(DailyInventory.builder()
                .date(today)
                .stockAvailable(1) // 재고 0개 (품절)
                .build());
        product4.addDailyInventory(DailyInventory.builder()
                .date(today)
                .stockAvailable(5) // 재고 5개
                .build());

        // store3 (GLAMPING)
        product5.addDailyInventory(DailyInventory.builder()
                .date(today)
                .stockAvailable(3) // 재고 3개
                .build());
        product6.addDailyInventory(DailyInventory.builder()
                .date(today)
                .stockAvailable(1) // 재고 1개
                .build());

        // Store에 Product 추가
        store1.addProduct(product1);
        store1.addProduct(product2);
        store2.addProduct(product3);
        store2.addProduct(product4);
        store3.addProduct(product5);
        store3.addProduct(product6);

        // Repository 저장
        return storeRepository.saveAll(List.of(store1, store2, store3));
    }
}