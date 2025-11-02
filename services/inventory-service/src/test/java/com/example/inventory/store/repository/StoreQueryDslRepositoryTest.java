package com.example.inventory.store.repository;

import com.example.inventory.product.repository.ProductRepository;
import com.example.inventory.store.dto.request.StoreUserFindRequest;
import com.example.inventory.product.entity.Product;
import com.example.inventory.store.entity.Address;
import com.example.inventory.store.entity.Amenities;
import com.example.inventory.store.entity.Category;
import com.example.inventory.store.entity.Store;
import jakarta.transaction.Transactional;
import org.assertj.core.groups.Tuple;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalTime;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

@ActiveProfiles("test")
@SpringBootTest
class StoreQueryDslRepositoryTest {

    @Autowired
    private StoreQueryDslRepository storeQueryDslRepository;

    @Autowired
    private StoreRepository storeRepository;

    @Autowired
    private ProductRepository productRepository;

    @AfterEach
    void tearDown() {
        productRepository.deleteAllInBatch();
        storeRepository.deleteAllInBatch();
    }

    @DisplayName("가게 전체 조회 (QueryDsl 적용)")
    @Test
    void findAllByFetchJoin2() {
        // given
        Store store1 = createStore("1", "testStore1", List.of(Amenities.PARKING, Amenities.BAR_LOUNGE), Category.HOTEL, "서울시", "강남구");
        Store store2 = createStore("2", "testStore2", List.of(Amenities.PARKING, Amenities.RESTAURANT), Category.PENSION, "서울시", "강남구");
        Product product1 = createProduct("product1", 1000);
        Product product2 = createProduct("product2", 2000);
        store1.addProduct(product1);
        store2.addProduct(product2);

        storeRepository.saveAll(List.of(store1, store2));

        StoreUserFindRequest storeUserFindRequest = new StoreUserFindRequest();

        // when
        Page<Store> stores = storeQueryDslRepository.findAllByFetchJoin(storeUserFindRequest, PageRequest.of(0, 3));

        // then
        assertThat(stores).hasSize(2)
                .extracting("name")
                .containsExactlyInAnyOrder("testStore1", "testStore2");
    }

    @Transactional
    @DisplayName("상품 전체조회시 부대시설도 같이 출력된다. (지연로딩)")
    @Test
    void findAllWithAmenities() {
        // given
        Store store1 = createStore("1", "testStore1", List.of(Amenities.PARKING, Amenities.BAR_LOUNGE), Category.HOTEL, "서울시", "강남구");
        Store store2 = createStore("1", "testStore2", List.of(Amenities.PARKING, Amenities.RESTAURANT), Category.PENSION, "서울시", "강남구");
        Store store3 = createStore("1", "testStore3", List.of(Amenities.BAR_LOUNGE, Amenities.BREAKFAST_INCLUDED), Category.GLAMPING, "서울시", "강남구");
        storeRepository.saveAll(List.of(store1, store2, store3));

        // when
        List<Store> stores = storeRepository.findAll();

        // then
        assertThat(stores).hasSize(3)
                .extracting("hostId", "name", "category")
                .containsExactlyInAnyOrder(
                        Tuple.tuple("1", "testStore1", Category.HOTEL),
                        Tuple.tuple("1", "testStore2", Category.PENSION),
                        Tuple.tuple("1", "testStore3", Category.GLAMPING)
                );
        assertThat(stores).satisfiesExactlyInAnyOrder(
                store -> assertThat(store.getAmenities()).containsExactlyInAnyOrder(Amenities.PARKING, Amenities.BAR_LOUNGE),
                store -> assertThat(store.getAmenities()).containsExactlyInAnyOrder(Amenities.PARKING, Amenities.RESTAURANT),
                store -> assertThat(store.getAmenities()).containsExactlyInAnyOrder(Amenities.BAR_LOUNGE, Amenities.BREAKFAST_INCLUDED)
        );
    }

    @Transactional
    @DisplayName("가게별 상품 2개를 넣으면 모든 가게와 상품들이 조회된다. (지연로딩)")
    @Test
    void findAllWithProducts() {
        // given
        Store store1 = createStore("1", "testStore1", List.of(Amenities.PARKING, Amenities.BAR_LOUNGE), Category.HOTEL, "서울시", "강남구");
        Store store2 = createStore("1", "testStore2", List.of(Amenities.PARKING, Amenities.RESTAURANT), Category.PENSION, "서울시", "강남구");
        Store store3 = createStore("1", "testStore3", List.of(Amenities.BAR_LOUNGE, Amenities.BREAKFAST_INCLUDED), Category.GLAMPING, "서울시", "강남구");
        store1.addProduct(createProduct("product1", 1000));
        store1.addProduct(createProduct("product2", 2000));
        store2.addProduct(createProduct("product3", 3000));
        store2.addProduct(createProduct("product4", 4000));
        store3.addProduct(createProduct("product5", 5000));
        store3.addProduct(createProduct("product6", 6000));

        storeRepository.saveAll(List.of(store1, store2, store3));

        // when & then
        List<Store> stores = storeRepository.findAll();

        assertThat(stores).hasSize(3)
                .satisfiesExactlyInAnyOrder(
                        store -> assertThat(store)
                                .extracting("hostId", "name")
                                .containsExactly("1", "testStore1")
                                .satisfies(s -> assertThat(store.getProducts())
                                        .extracting("name") // List<Product> -> List<String> 변환
                                        .containsExactlyInAnyOrder("product1", "product2")),
                        store -> assertThat(store)
                                .extracting("hostId", "name")
                                .containsExactly("1", "testStore2")
                                .satisfies(s -> assertThat(store.getProducts())
                                        .extracting("name") // List<Product> -> List<String> 변환
                                        .containsExactlyInAnyOrder("product3", "product4")),
                        store -> assertThat(store)
                                .extracting("hostId", "name")
                                .containsExactly("1", "testStore3")
                                .satisfies(s -> assertThat(store.getProducts())
                                        .extracting("name") // List<Product> -> List<String> 변환
                                        .containsExactlyInAnyOrder("product5", "product6"))
                );
    }

    @DisplayName("가게별 상품 2개를 넣으면 모든 가게와 상품들이 조회된다. (N+1 해결)")
    @Test
    void findAllWithProductsNPlusOne() {
        // given
        Store store1 = createStore("1", "testStore1", List.of(Amenities.PARKING, Amenities.BAR_LOUNGE), Category.HOTEL, "서울시", "강남구");
        Store store2 = createStore("1", "testStore2", List.of(Amenities.PARKING, Amenities.RESTAURANT), Category.PENSION, "서울시", "강남구");
        Store store3 = createStore("1", "testStore3", List.of(Amenities.BAR_LOUNGE, Amenities.BREAKFAST_INCLUDED), Category.GLAMPING, "서울시", "강남구");
        store1.addProduct(createProduct("product1", 1000));
        store1.addProduct(createProduct("product2", 2000));
        store2.addProduct(createProduct("product3", 3000));
        store2.addProduct(createProduct("product4", 4000));
        store3.addProduct(createProduct("product5", 5000));
        store3.addProduct(createProduct("product6", 6000));

        storeRepository.saveAll(List.of(store1, store2, store3));

        StoreUserFindRequest storeUserFindRequest = new StoreUserFindRequest();
        // when
        Page<Store> stores = storeQueryDslRepository.findAllByFetchJoin(storeUserFindRequest, PageRequest.of(0, 3));
        // then
        assertThat(stores).hasSize(3)
                .satisfiesExactlyInAnyOrder(
                        store -> assertThat(store)
                                .extracting("hostId", "name")
                                .containsExactly("1", "testStore1")
                                .satisfies(s -> assertThat(store.getProducts())
                                        .extracting("name") // List<Product> -> List<String> 변환
                                        .containsExactlyInAnyOrder("product1", "product2")),
                        store -> assertThat(store)
                                .extracting("hostId", "name")
                                .containsExactly("1", "testStore2")
                                .satisfies(s -> assertThat(store.getProducts())
                                        .extracting("name") // List<Product> -> List<String> 변환
                                        .containsExactlyInAnyOrder("product3", "product4")),
                        store -> assertThat(store)
                                .extracting("hostId", "name")
                                .containsExactly("1", "testStore3")
                                .satisfies(s -> assertThat(store.getProducts())
                                        .extracting("name") // List<Product> -> List<String> 변환
                                        .containsExactlyInAnyOrder("product5", "product6"))
                );
    }

    @DisplayName("가게별 상품과 부대시설을 함께 조회한다.")
    @Test
    void findAllWithProductsAndAmenities() {
        // given
        Store store1 = createStore("1", "testStore1", List.of(Amenities.PARKING, Amenities.BAR_LOUNGE), Category.HOTEL, "서울시", "강남구");
        Store store2 = createStore("1", "testStore2", List.of(Amenities.PARKING, Amenities.RESTAURANT), Category.PENSION, "서울시", "강남구");
        Store store3 = createStore("1", "testStore3", List.of(Amenities.BAR_LOUNGE, Amenities.BREAKFAST_INCLUDED), Category.GLAMPING, "서울시", "강남구");
        store1.addProduct(createProduct("product1", 1000));
        store1.addProduct(createProduct("product2", 2000));
        store2.addProduct(createProduct("product3", 3000));
        store2.addProduct(createProduct("product4", 4000));
        store3.addProduct(createProduct("product5", 5000));
        store3.addProduct(createProduct("product6", 6000));

        storeRepository.saveAll(List.of(store1, store2, store3));

        StoreUserFindRequest storeUserFindRequest = new StoreUserFindRequest();
        // when
        Page<Store> stores = storeQueryDslRepository.findAllByFetchJoin(storeUserFindRequest, PageRequest.of(0, 3));

        // then
        assertThat(stores).hasSize(3)
                .satisfiesExactlyInAnyOrder(
                        store -> assertThat(store)
                                .extracting("hostId", "name")
                                .containsExactly("1", "testStore1")
                                .satisfies(s -> assertThat(store.getAmenities())
                                        .containsExactlyInAnyOrder(Amenities.PARKING, Amenities.BAR_LOUNGE))
                                .satisfies(s -> assertThat(store.getProducts())
                                        .extracting("name") // List<Product> -> List<String> 변환
                                        .containsExactlyInAnyOrder("product1", "product2")),
                        store -> assertThat(store)
                                .extracting("hostId", "name")
                                .containsExactly("1", "testStore2")
                                .satisfies(s -> assertThat(store.getAmenities())
                                        .containsExactlyInAnyOrder(Amenities.PARKING, Amenities.RESTAURANT))
                                .satisfies(s -> assertThat(store.getProducts())
                                        .extracting("name") // List<Product> -> List<String> 변환
                                        .containsExactlyInAnyOrder("product3", "product4")),
                        store -> assertThat(store)
                                .extracting("hostId", "name")
                                .containsExactly("1", "testStore3")
                                .satisfies(s -> assertThat(store.getAmenities())
                                        .containsExactlyInAnyOrder(Amenities.BAR_LOUNGE, Amenities.BREAKFAST_INCLUDED))
                                .satisfies(s -> assertThat(store.getProducts())
                                        .extracting("name") // List<Product> -> List<String> 변환
                                        .containsExactlyInAnyOrder("product5", "product6"))
                );
    }

    @DisplayName("필터링에 이름을 추가하였을때 검색한 이름이 포함된 경우 조회된다.")
    @Test
    void findStoreByName() {
        // given
        Store store1 = createStore("1", "경화수월", List.of(Amenities.PARKING, Amenities.BAR_LOUNGE), Category.HOTEL, "서울시", "강남구");
        Store store2 = createStore("1", "경화수화", List.of(Amenities.PARKING, Amenities.RESTAURANT), Category.PENSION, "서울시", "강남구");
        Store store3 = createStore("1", "testStore", List.of(Amenities.BAR_LOUNGE, Amenities.BREAKFAST_INCLUDED), Category.GLAMPING, "서울시", "강남구");
        store1.addProduct(createProduct("product1", 1000));
        store1.addProduct(createProduct("product2", 2000));
        store2.addProduct(createProduct("product3", 3000));
        store2.addProduct(createProduct("product4", 4000));
        store3.addProduct(createProduct("product5", 5000));
        store3.addProduct(createProduct("product6", 6000));

        storeRepository.saveAll(List.of(store1, store2, store3));

        StoreUserFindRequest request = StoreUserFindRequest.builder()
                .name("경화수")
                .build();

        // when
        Page<Store> stores = storeQueryDslRepository.findAllByFetchJoin(request, PageRequest.of(0, 3));

        // then
        assertThat(stores).hasSize(2)
                .extracting("name")
                .containsExactlyInAnyOrder(
                        "경화수월", "경화수화"
                );
    }

    @DisplayName("선택한 카테고리에 해당되는 가게들이 조회된다.")
    @Test
    void findStoreByCategory() {
        Store store1 = createStore("1", "경화수월", List.of(Amenities.PARKING, Amenities.BAR_LOUNGE), Category.HOTEL, "서울시", "강남구");
        Store store2 = createStore("1", "경화수화", List.of(Amenities.PARKING, Amenities.RESTAURANT), Category.PENSION, "서울시", "강남구");
        Store store3 = createStore("1", "testStore", List.of(Amenities.BAR_LOUNGE, Amenities.BREAKFAST_INCLUDED), Category.GLAMPING, "서울시", "강남구");
        store1.addProduct(createProduct("product1", 1000));
        store1.addProduct(createProduct("product2", 2000));
        store2.addProduct(createProduct("product3", 3000));
        store2.addProduct(createProduct("product4", 4000));
        store3.addProduct(createProduct("product5", 5000));
        store3.addProduct(createProduct("product6", 6000));

        storeRepository.saveAll(List.of(store1, store2, store3));

        StoreUserFindRequest request = StoreUserFindRequest.builder()
                .category(Category.HOTEL)
                .build();

        // when
        Page<Store> stores = storeQueryDslRepository.findAllByFetchJoin(request, PageRequest.of(0, 3));

        // then
        assertThat(stores).hasSize(1)
                .extracting("name")
                .contains("경화수월");
    }

    @DisplayName("주소를 입력하면 주소에 해당하는 가게가 조회됩니다.")
    @Test
    void findStoreByAddress() {
        // given
        Store store1 = createStore("1", "경화수월", List.of(Amenities.PARKING, Amenities.BAR_LOUNGE), Category.HOTEL, "서울시", "강남구");
        Store store2 = createStore("1", "경화수화", List.of(Amenities.PARKING, Amenities.RESTAURANT), Category.PENSION, "경기도", "시흥시");
        Store store3 = createStore("1", "testStore", List.of(Amenities.BAR_LOUNGE, Amenities.BREAKFAST_INCLUDED), Category.GLAMPING, "서울시", "강남구");
        store1.addProduct(createProduct("product1", 1000));
        store1.addProduct(createProduct("product2", 2000));
        store2.addProduct(createProduct("product3", 3000));
        store2.addProduct(createProduct("product4", 4000));
        store3.addProduct(createProduct("product5", 5000));
        store3.addProduct(createProduct("product6", 6000));

        storeRepository.saveAll(List.of(store1, store2, store3));

        StoreUserFindRequest request = StoreUserFindRequest.builder()
                .province("서울시")
                .city("강남구")
                .build();

        // when
        Page<Store> stores = storeQueryDslRepository.findAllByFetchJoin(request, PageRequest.of(0, 3));

        // then
        assertThat(stores).hasSize(2)
                .extracting("name")
                .containsExactlyInAnyOrder("경화수월", "testStore");
    }

    @DisplayName("부대시설 선택시 해당하는 가게들이 조회된다.")
    @Test
    void findStoreByAmenities() {
        // given
        Store store1 = createStore("1", "경화수월", List.of(Amenities.PARKING, Amenities.BAR_LOUNGE), Category.HOTEL, "서울시", "강남구");
        Store store2 = createStore("1", "경화수화", List.of(Amenities.PARKING, Amenities.RESTAURANT), Category.PENSION, "경기도", "시흥시");
        Store store3 = createStore("1", "testStore", List.of(Amenities.BAR_LOUNGE, Amenities.BREAKFAST_INCLUDED), Category.GLAMPING, "서울시", "강남구");
        store1.addProduct(createProduct("product1", 1000));
        store1.addProduct(createProduct("product2", 2000));
        store2.addProduct(createProduct("product3", 3000));
        store2.addProduct(createProduct("product4", 4000));
        store3.addProduct(createProduct("product5", 5000));
        store3.addProduct(createProduct("product6", 6000));

        storeRepository.saveAll(List.of(store1, store2, store3));

        StoreUserFindRequest request = StoreUserFindRequest.builder()
                .amenities(List.of(Amenities.PARKING, Amenities.RESTAURANT))
                .build();

        // when
        Page<Store> stores = storeQueryDslRepository.findAllByFetchJoin(request, PageRequest.of(0, 3));

        // then
        assertThat(stores).hasSize(1)
                .extracting("name")
                .containsExactlyInAnyOrder("경화수화");
    }

    private Product createProduct(String name, int price) {
        return Product.builder()
                .name(name)
                .basePrice(price)
                .build();
    }

    private Store createStore(String hostId, String name, List<Amenities> amenities, Category category, String province, String city) {
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

}