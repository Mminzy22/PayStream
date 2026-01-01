package com.paystream.inventory.store.repository;

import static org.assertj.core.api.Assertions.*;

import com.paystream.inventory.inventory.entity.DailyInventory;
import com.paystream.inventory.product.entity.Product;
import com.paystream.inventory.product.repository.ProductRepository;
import com.paystream.inventory.store.dto.request.StoreListFindRequest;
import com.paystream.inventory.store.entity.Address;
import com.paystream.inventory.store.entity.Amenities;
import com.paystream.inventory.store.entity.Category;
import com.paystream.inventory.store.entity.Store;
import jakarta.transaction.Transactional;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.assertj.core.groups.Tuple;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;

@Slf4j
@Transactional
@ActiveProfiles("test")
@SpringBootTest
class StoreQueryDslRepositoryTest {

    @Autowired private StoreQueryDslRepository storeQueryDslRepository;

    @Autowired private StoreRepository storeRepository;

    @Autowired private ProductRepository productRepository;

    @DisplayName("가게 전체 조회 (QueryDsl 적용)")
    @Test
    void findAllByFetchJoin2() {
        // given
        Store store1 =
                createStore(
                        "1",
                        "testStore1",
                        List.of(Amenities.PARKING, Amenities.BAR_LOUNGE),
                        Category.HOTEL,
                        "서울시",
                        "강남구");
        Store store2 =
                createStore(
                        "2",
                        "testStore2",
                        List.of(Amenities.PARKING, Amenities.RESTAURANT),
                        Category.PENSION,
                        "서울시",
                        "강남구");
        Product product1 = createProduct("product1", 1000, 2);
        Product product2 = createProduct("product2", 2000, 2);

        product1.addDailyInventory(createDailyInventory(LocalDate.now(), 1));
        product1.addDailyInventory(createDailyInventory(LocalDate.now().plusDays(1), 1));
        product2.addDailyInventory(createDailyInventory(LocalDate.now(), 1));
        product2.addDailyInventory(createDailyInventory(LocalDate.now().plusDays(1), 1));

        store1.addProduct(product1);
        store2.addProduct(product2);

        storeRepository.saveAll(List.of(store1, store2));

        LocalDate today = LocalDate.now();
        StoreListFindRequest storeListFindRequest =
                StoreListFindRequest.builder()
                        .checkInDate(today)
                        .checkOutDate(today.plusDays(1))
                        .personCount(2)
                        .build();

        // when
        Page<Store> stores =
                storeQueryDslRepository.findAllByFetchJoin(
                        storeListFindRequest, PageRequest.of(0, 3));

        // then
        assertThat(stores)
                .hasSize(2)
                .extracting("name")
                .containsExactlyInAnyOrder("testStore1", "testStore2");
    }

    @Transactional
    @DisplayName("상품 전체조회시 부대시설도 같이 출력된다. (지연로딩)")
    @Test
    void findAllWithAmenities() {
        // given
        Store store1 =
                createStore(
                        "1",
                        "testStore1",
                        List.of(Amenities.PARKING, Amenities.BAR_LOUNGE),
                        Category.HOTEL,
                        "서울시",
                        "강남구");
        Store store2 =
                createStore(
                        "1",
                        "testStore2",
                        List.of(Amenities.PARKING, Amenities.RESTAURANT),
                        Category.PENSION,
                        "서울시",
                        "강남구");
        Store store3 =
                createStore(
                        "1",
                        "testStore3",
                        List.of(Amenities.BAR_LOUNGE, Amenities.BREAKFAST_INCLUDED),
                        Category.GLAMPING,
                        "서울시",
                        "강남구");
        storeRepository.saveAll(List.of(store1, store2, store3));

        // when
        List<Store> stores = storeRepository.findAll();

        // then
        assertThat(stores)
                .hasSize(3)
                .extracting("hostId", "name", "category")
                .containsExactlyInAnyOrder(
                        Tuple.tuple("1", "testStore1", Category.HOTEL),
                        Tuple.tuple("1", "testStore2", Category.PENSION),
                        Tuple.tuple("1", "testStore3", Category.GLAMPING));
        assertThat(stores)
                .satisfiesExactlyInAnyOrder(
                        store ->
                                assertThat(store.getAmenities())
                                        .containsExactlyInAnyOrder(
                                                Amenities.PARKING, Amenities.BAR_LOUNGE),
                        store ->
                                assertThat(store.getAmenities())
                                        .containsExactlyInAnyOrder(
                                                Amenities.PARKING, Amenities.RESTAURANT),
                        store ->
                                assertThat(store.getAmenities())
                                        .containsExactlyInAnyOrder(
                                                Amenities.BAR_LOUNGE,
                                                Amenities.BREAKFAST_INCLUDED));
    }

    @DisplayName("가게별 상품 2개를 넣으면 모든 가게와 상품들이 조회된다. (N+1 해결)")
    @Test
    void findAllWithProductsNPlusOne() {
        // given
        createTemplate();

        LocalDate today = LocalDate.now();
        StoreListFindRequest storeListFindRequest =
                StoreListFindRequest.builder()
                        .checkInDate(today)
                        .checkOutDate(today.plusDays(1))
                        .personCount(2)
                        .build();
        // when
        Page<Store> stores =
                storeQueryDslRepository.findAllByFetchJoin(
                        storeListFindRequest, PageRequest.of(0, 3));

        for (Store store : stores) {
            log.info("store {}", store);
            store.getProducts().forEach(product -> log.info("product {}", product.getId()));
        }

        // then
        assertThat(stores)
                .hasSize(3)
                .satisfiesExactlyInAnyOrder(
                        store ->
                                assertThat(store)
                                        .extracting("hostId", "name")
                                        .containsExactly("1", "경화수월")
                                        .satisfies(
                                                s ->
                                                        assertThat(store.getProducts())
                                                                .extracting(
                                                                        "name") // List<Product> ->
                                                                // List<String> 변환
                                                                .containsExactlyInAnyOrder(
                                                                        "product1", "product2")),
                        store ->
                                assertThat(store)
                                        .extracting("hostId", "name")
                                        .containsExactly("1", "경화수화")
                                        .satisfies(
                                                s ->
                                                        assertThat(store.getProducts())
                                                                .extracting(
                                                                        "name") // List<Product> ->
                                                                // List<String> 변환
                                                                .containsExactlyInAnyOrder(
                                                                        "product3", "product4")),
                        store ->
                                assertThat(store)
                                        .extracting("hostId", "name")
                                        .containsExactly("1", "경화수수")
                                        .satisfies(
                                                s ->
                                                        assertThat(store.getProducts())
                                                                .extracting(
                                                                        "name") // List<Product> ->
                                                                // List<String> 변환
                                                                .containsExactlyInAnyOrder(
                                                                        "product5", "product6")));
    }

    @DisplayName("가게별 상품과 부대시설을 함께 조회한다.")
    @Test
    void findAllWithProductsAndAmenities() {
        // given
        createTemplate();

        LocalDate today = LocalDate.now();
        StoreListFindRequest storeListFindRequest =
                StoreListFindRequest.builder()
                        .checkInDate(today)
                        .checkOutDate(today.plusDays(1))
                        .personCount(2)
                        .build();
        // when
        Page<Store> stores =
                storeQueryDslRepository.findAllByFetchJoin(
                        storeListFindRequest, PageRequest.of(0, 3));

        // then
        assertThat(stores.getContent())
                .hasSize(3)
                .satisfiesExactlyInAnyOrder(
                        store ->
                                assertThat(store)
                                        .extracting("hostId", "name")
                                        .containsExactly("1", "경화수월")
                                        .satisfies(
                                                s ->
                                                        assertThat(store.getAmenities())
                                                                .containsExactlyInAnyOrder(
                                                                        Amenities.PARKING,
                                                                        Amenities.BAR_LOUNGE))
                                        .satisfies(
                                                s ->
                                                        assertThat(store.getProducts())
                                                                .extracting(
                                                                        "name") // List<Product> ->
                                                                // List<String> 변환
                                                                .containsExactlyInAnyOrder(
                                                                        "product1", "product2")),
                        store ->
                                assertThat(store)
                                        .extracting("hostId", "name")
                                        .containsExactly("1", "경화수화")
                                        .satisfies(
                                                s ->
                                                        assertThat(store.getAmenities())
                                                                .containsExactlyInAnyOrder(
                                                                        Amenities.PARKING,
                                                                        Amenities.RESTAURANT))
                                        .satisfies(
                                                s ->
                                                        assertThat(store.getProducts())
                                                                .extracting(
                                                                        "name") // List<Product> ->
                                                                // List<String> 변환
                                                                .containsExactlyInAnyOrder(
                                                                        "product3", "product4")),
                        store ->
                                assertThat(store)
                                        .extracting("hostId", "name")
                                        .containsExactly("1", "경화수수")
                                        .satisfies(
                                                s ->
                                                        assertThat(store.getAmenities())
                                                                .containsExactlyInAnyOrder(
                                                                        Amenities.BAR_LOUNGE,
                                                                        Amenities
                                                                                .BREAKFAST_INCLUDED))
                                        .satisfies(
                                                s ->
                                                        assertThat(store.getProducts())
                                                                .extracting(
                                                                        "name") // List<Product> ->
                                                                // List<String> 변환
                                                                .containsExactlyInAnyOrder(
                                                                        "product5", "product6")));
    }

    @DisplayName("필터링에 이름을 추가하였을때 검색한 이름이 포함된 경우 조회된다.")
    @Test
    void findStoreByName() {
        // given
        createTemplate();

        LocalDate today = LocalDate.now();
        StoreListFindRequest request =
                StoreListFindRequest.builder()
                        .name("경화수")
                        .checkInDate(today)
                        .checkOutDate(today.plusDays(1))
                        .personCount(2)
                        .build();

        // when
        Page<Store> stores =
                storeQueryDslRepository.findAllByFetchJoin(request, PageRequest.of(0, 3));

        // then
        assertThat(stores)
                .hasSize(3)
                .extracting("name")
                .containsExactlyInAnyOrder("경화수월", "경화수화", "경화수수");
    }

    @DisplayName("선택한 카테고리에 해당되는 가게들이 조회된다.")
    @Test
    void findStoreByCategory() {
        createTemplate();

        LocalDate today = LocalDate.now();
        StoreListFindRequest request =
                StoreListFindRequest.builder()
                        .name("경화수")
                        .checkInDate(today)
                        .checkOutDate(today.plusDays(1))
                        .personCount(2)
                        .category(Category.HOTEL)
                        .build();

        // when
        Page<Store> stores =
                storeQueryDslRepository.findAllByFetchJoin(request, PageRequest.of(0, 3));

        // then
        assertThat(stores).hasSize(1).extracting("name").contains("경화수월");
    }

    @DisplayName("주소를 입력하면 주소에 해당하는 가게가 조회됩니다.")
    @Test
    void findStoreByAddress() {
        // given
        createTemplate();

        LocalDate today = LocalDate.now();
        StoreListFindRequest request =
                StoreListFindRequest.builder()
                        .province("서울시")
                        .city("강남구")
                        .checkInDate(today)
                        .checkOutDate(today.plusDays(1))
                        .personCount(2)
                        .build();

        // when
        Page<Store> stores =
                storeQueryDslRepository.findAllByFetchJoin(request, PageRequest.of(0, 3));
        // then
        assertThat(stores).hasSize(2).extracting("name").containsExactlyInAnyOrder("경화수월", "경화수수");
    }

    @DisplayName("부대시설 선택시 해당하는 가게들이 조회된다.")
    @Test
    void findStoreByAmenities() {
        // given
        createTemplate();

        LocalDate today = LocalDate.now();
        StoreListFindRequest request =
                StoreListFindRequest.builder()
                        .amenities(List.of(Amenities.PARKING, Amenities.RESTAURANT))
                        .checkInDate(today)
                        .checkOutDate(today.plusDays(1))
                        .personCount(2)
                        .build();

        // when
        Page<Store> stores =
                storeQueryDslRepository.findAllByFetchJoin(request, PageRequest.of(0, 3));

        // then
        assertThat(stores).hasSize(1).extracting("name").containsExactlyInAnyOrder("경화수화");
    }

    @DisplayName("인원수 초과시 해당 가게는 조회되지 않는다.")
    @Test
    void findAllStoreWithOutPersonCountOver() {
        // given
        Store store1 =
                createStore(
                        "1",
                        "경화수월",
                        List.of(Amenities.PARKING, Amenities.BAR_LOUNGE),
                        Category.HOTEL,
                        "서울시",
                        "강남구");
        Store store2 =
                createStore(
                        "1",
                        "경화수화",
                        List.of(Amenities.PARKING, Amenities.RESTAURANT),
                        Category.PENSION,
                        "경기도",
                        "시흥시");
        Store store3 =
                createStore(
                        "1",
                        "경화수수",
                        List.of(Amenities.BAR_LOUNGE, Amenities.BREAKFAST_INCLUDED),
                        Category.GLAMPING,
                        "서울시",
                        "강남구");
        Product product1 = createProduct("product1", 1000, 2);
        Product product2 = createProduct("product2", 2000, 2);
        Product product3 = createProduct("product3", 3000, 2);
        Product product4 = createProduct("product4", 4000, 2);
        Product product5 = createProduct("product5", 5000, 2);
        Product product6 = createProduct("product6", 6000, 2);

        product1.addDailyInventory(createDailyInventory(LocalDate.now(), 1));
        product1.addDailyInventory(createDailyInventory(LocalDate.now().plusDays(1), 1));
        product2.addDailyInventory(createDailyInventory(LocalDate.now(), 1));
        product2.addDailyInventory(createDailyInventory(LocalDate.now().plusDays(1), 1));

        product3.addDailyInventory(createDailyInventory(LocalDate.now(), 1));
        product3.addDailyInventory(createDailyInventory(LocalDate.now().plusDays(1), 1));
        product4.addDailyInventory(createDailyInventory(LocalDate.now(), 1));
        product4.addDailyInventory(createDailyInventory(LocalDate.now().plusDays(1), 1));

        product5.addDailyInventory(createDailyInventory(LocalDate.now(), 1));
        product5.addDailyInventory(createDailyInventory(LocalDate.now().plusDays(1), 1));
        product6.addDailyInventory(createDailyInventory(LocalDate.now(), 0)); // product6 조회 x
        product6.addDailyInventory(createDailyInventory(LocalDate.now().plusDays(1), 1));

        store1.addProduct(product1);
        store1.addProduct(product2);
        store2.addProduct(product3);
        store2.addProduct(product4);
        store3.addProduct(product5);
        store3.addProduct(product6);

        storeRepository.saveAll(List.of(store1, store2, store3));

        LocalDate today = LocalDate.now();
        StoreListFindRequest request =
                StoreListFindRequest.builder()
                        .checkInDate(today)
                        .checkOutDate(today.plusDays(1))
                        .personCount(2)
                        .build();

        // when
        List<Store> result =
                storeQueryDslRepository.findAllByFetchJoin(request, PageRequest.of(0, 3)).stream()
                        .toList();

        // then
        assertThat(result).hasSize(3);
    }

    @DisplayName("가게 상세조회가 정상적으로 조회된다.")
    @Test
    void findOne() {
        // given
        Store store1 =
                createStore(
                        "1",
                        "경화수월",
                        List.of(Amenities.PARKING, Amenities.BAR_LOUNGE),
                        Category.HOTEL,
                        "서울시",
                        "강남구");
        Product product1 = createProduct("product1", 1000, 2);
        Product product2 = createProduct("product2", 2000, 2);

        store1.addProduct(product1);
        store1.addProduct(product2);

        product1.addDailyInventory(createDailyInventory(LocalDate.now(), 1));
        product1.addDailyInventory(createDailyInventory(LocalDate.now().plusDays(1), 1));
        product2.addDailyInventory(createDailyInventory(LocalDate.now(), 1));
        product2.addDailyInventory(createDailyInventory(LocalDate.now().plusDays(1), 1));

        Store savedStore = storeRepository.save(store1);

        // when
        Store findStore =
                storeQueryDslRepository
                        .findOne(savedStore.getId(), LocalDate.now(), LocalDate.now().plusDays(1))
                        .orElse(null);

        // then
        Assertions.assertNotNull(findStore);
        assertThat(findStore.getProducts())
                .hasSize(2)
                .extracting("name", "basePrice", "maxPersonCount")
                .containsExactly(
                        Tuple.tuple("product1", 1000, 2), Tuple.tuple("product2", 2000, 2));
    }

    private List<Store> createTemplate() {
        Store store1 =
                createStore(
                        "1",
                        "경화수월",
                        List.of(Amenities.PARKING, Amenities.BAR_LOUNGE),
                        Category.HOTEL,
                        "서울시",
                        "강남구");
        Store store2 =
                createStore(
                        "1",
                        "경화수화",
                        List.of(Amenities.PARKING, Amenities.RESTAURANT),
                        Category.PENSION,
                        "경기도",
                        "시흥시");
        Store store3 =
                createStore(
                        "1",
                        "경화수수",
                        List.of(Amenities.BAR_LOUNGE, Amenities.BREAKFAST_INCLUDED),
                        Category.GLAMPING,
                        "서울시",
                        "강남구");

        Product product1 = createProduct("product1", 1000, 2);
        Product product2 = createProduct("product2", 2000, 2);
        Product product3 = createProduct("product3", 3000, 2);
        Product product4 = createProduct("product4", 4000, 2);
        Product product5 = createProduct("product5", 5000, 2);
        Product product6 = createProduct("product6", 6000, 2);

        product1.addDailyInventory(createDailyInventory(LocalDate.now(), 1));
        product1.addDailyInventory(createDailyInventory(LocalDate.now().plusDays(1), 1));
        product2.addDailyInventory(createDailyInventory(LocalDate.now(), 1));
        product2.addDailyInventory(createDailyInventory(LocalDate.now().plusDays(1), 1));

        product3.addDailyInventory(createDailyInventory(LocalDate.now(), 1));
        product3.addDailyInventory(createDailyInventory(LocalDate.now().plusDays(1), 1));
        product4.addDailyInventory(createDailyInventory(LocalDate.now(), 1));
        product4.addDailyInventory(createDailyInventory(LocalDate.now().plusDays(1), 1));

        product5.addDailyInventory(createDailyInventory(LocalDate.now(), 1));
        product5.addDailyInventory(createDailyInventory(LocalDate.now().plusDays(1), 1));
        product6.addDailyInventory(createDailyInventory(LocalDate.now(), 1)); // product6 조회 x
        product6.addDailyInventory(createDailyInventory(LocalDate.now().plusDays(1), 1));

        store1.addProduct(product1);
        store1.addProduct(product2);
        store2.addProduct(product3);
        store2.addProduct(product4);
        store3.addProduct(product5);
        store3.addProduct(product6);

        return storeRepository.saveAll(List.of(store1, store2, store3));
    }

    private Store createStore(
            String hostId,
            String name,
            List<Amenities> amenities,
            Category category,
            String province,
            String city) {
        return Store.builder()
                .hostId(hostId)
                .name(name)
                .address(new Address(province, city))
                .category(category)
                .amenities(amenities)
                .checkInTime(LocalTime.now())
                .checkOutTime(LocalTime.now())
                .build();
    }

    private Product createProduct(String name, int price, int maxPersonCount) {
        return Product.builder()
                .name(name)
                .description("test")
                .basePrice(price)
                .maxPersonCount(maxPersonCount)
                .build();
    }

    private DailyInventory createDailyInventory(LocalDate date, int stock) {
        return DailyInventory.builder().date(date).stockAvailable(stock).build();
    }
}
