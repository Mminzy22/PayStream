package com.example.inventory.service;

import com.example.inventory.dto.store.StoreResponse;
import com.example.inventory.dto.store.request.StoreUserFindRequest;
import com.example.inventory.entity.inventory.DailyInventory;
import com.example.inventory.entity.product.Product;
import com.example.inventory.entity.store.Address;
import com.example.inventory.entity.store.Amenities;
import com.example.inventory.entity.store.Category;
import com.example.inventory.entity.store.Store;
import com.example.inventory.repository.DailyInventoryRepository;
import com.example.inventory.repository.ProductRepository;
import com.example.inventory.repository.StoreRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.groups.Tuple.tuple;

//@Transactional
@ActiveProfiles("test")
@SpringBootTest
class StoreServiceTest {

    @Autowired
    private StoreService storeService;

    @Autowired
    private StoreRepository storeRepository;
    @Autowired
    private ProductRepository productRepository;
    @Autowired
    private DailyInventoryRepository dailyInventoryRepository;

    @EnableJpaAuditing
    @TestConfiguration
    static class TestConfig {

    }

    @AfterEach
    void tearDown() {
        dailyInventoryRepository.deleteAllInBatch();
        productRepository.deleteAllInBatch();
        storeRepository.deleteAllInBatch();
    }

    @DisplayName("가게 전체 조회시 상품의 최저 금액이 같이 조회된다.")
    @Test
    void findAllWithMinPrice() {
        // given
        createTemplate();

        StoreUserFindRequest request = StoreUserFindRequest.builder()
                .checkIn(LocalDate.now())
                .checkOut(LocalDate.now().plusDays(1))
                .build();

        // when
        List<StoreResponse> response = storeService.userFindStoreList(request, PageRequest.of(1, 3));

        // then
        assertThat(response).hasSize(3)
                .extracting("name", "minPrice")
                .containsExactlyInAnyOrder(
                        tuple("testStore1", 1000),
                        tuple("testStore2", 3000),
                        tuple("testStore3", 5000)
                );
    }

    @DisplayName("가게 전체 조회 with 페이징")
    @Test
    void findAllWithPaging() {
        // given
        int page = 1;
        int size = 2;
        createTemplate();

        StoreUserFindRequest request = StoreUserFindRequest.builder()
                .checkIn(LocalDate.now())
                .checkOut(LocalDate.now().plusDays(1))
                .build();

        // when
        List<StoreResponse> responses = storeService.userFindStoreList(request, PageRequest.of(page, size));

        // then
        assertThat(responses).hasSize(size)
                .extracting("name", "minPrice")
                .containsExactlyInAnyOrder(
                        tuple("testStore1", 1000),
                        tuple("testStore2", 3000)
                );
    }

    @DisplayName("재고가 있는 상품의 가게만 조회된다.")
    @Test
    void findAllWithAvailableStock() {
        // given
        Store store1 = createStore("1", "testStore1", List.of(Amenities.PARKING, Amenities.BAR_LOUNGE), Category.HOTEL);
        Store store2 = createStore("1", "testStore2", List.of(Amenities.PARKING, Amenities.RESTAURANT), Category.PENSION);
        Product product1 = createProduct("product1", 1000);
        Product product2 = createProduct("product2", 2000);
        store1.addProduct(product1);
        store2.addProduct(product2);
        product1.addDailyInventory(DailyInventory.builder()
                        .date(LocalDate.now())
                        .stockAvailable(1)
                        .build());
        product2.addDailyInventory(DailyInventory.builder()
                .date(LocalDate.now())
                .stockAvailable(0)
                .build());

        storeRepository.saveAll(List.of(store1, store2));

        LocalDate checkIn = LocalDate.now();
        LocalDate checkOut = LocalDate.now().plusDays(1);

        StoreUserFindRequest request = StoreUserFindRequest.builder()
                .checkIn(checkIn)
                .checkOut(checkOut)
                .build();

        // when
        List<StoreResponse> response = storeService.userFindStoreList(request, PageRequest.of(1, 3));

        // then
        assertThat(response).hasSize(1)
                .extracting("name", "minPrice")
                .contains(tuple("testStore1", 1000));
    }

    @DisplayName("기간을 더 늘렷을때 정상적으로 조회되는지 확인")
    @Test
    void findAllWithAvailableStock2() {
        // given
        Store store1 = createStore("1", "testStore1", List.of(Amenities.PARKING, Amenities.BAR_LOUNGE), Category.HOTEL);
        Store store2 = createStore("2", "testStore2", List.of(Amenities.PARKING, Amenities.RESTAURANT), Category.PENSION);
        Product product1 = createProduct("product1", 1000);
        Product product2 = createProduct("product2", 2000);
        store1.addProduct(product1);
        store2.addProduct(product2);
        product1.addDailyInventory(DailyInventory.builder()
                .date(LocalDate.now())
                .stockAvailable(1)
                .build());
        product1.addDailyInventory(DailyInventory.builder()
                .date(LocalDate.now().plusDays(1))
                .stockAvailable(1)
                .build());
        product1.addDailyInventory(DailyInventory.builder()
                .date(LocalDate.now().plusDays(2))
                .stockAvailable(1)
                .build());
        product2.addDailyInventory(DailyInventory.builder()
                .date(LocalDate.now())
                .stockAvailable(0)
                .build());
        product2.addDailyInventory(DailyInventory.builder()
                .date(LocalDate.now().plusDays(1))
                .stockAvailable(0)
                .build());
        product2.addDailyInventory(DailyInventory.builder()
                .date(LocalDate.now().plusDays(2))
                .stockAvailable(1)
                .build());

        List<Store> stores = storeRepository.saveAll(List.of(store1, store2));
        for (Store store : stores) {
            System.out.println("store = " + store);
        }

        LocalDate checkIn = LocalDate.now().plusDays(1);
        LocalDate checkOut = LocalDate.now().plusDays(3);

        StoreUserFindRequest request = StoreUserFindRequest.builder()
                .checkIn(checkIn)
                .checkOut(checkOut)
                .build();

        // when
        List<StoreResponse> response = storeService.userFindStoreList(request, PageRequest.of(1, 3));
        for (StoreResponse storeResponse : response) {
            System.out.println("storeResponse = " + storeResponse);
        }
        
        // then
        assertThat(response).hasSize(1)
                .extracting("name", "minPrice")
                .contains(tuple("testStore1", 1000));
    }

    private void createTemplate() {
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
        storeRepository.saveAll(List.of(store1, store2, store3));
    }

    private Product createProduct(String name, int price) {
        return Product.builder()
                .name(name)
                .basePrice(price)
                .build();
    }

    private Store createStore(String hostId, String name, List<Amenities> amenities, Category category) {
        return Store.builder()
                .hostId(hostId)
                .name(name)
                .address(new Address("서울시", "강남구"))
                .category(category)
                .amenities(amenities)
                .checkInTime(LocalTime.now())
                .checkOutTime(LocalTime.now())
                .build();
    }

}