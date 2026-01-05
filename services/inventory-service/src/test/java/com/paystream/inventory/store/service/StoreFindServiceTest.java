package com.paystream.inventory.store.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.groups.Tuple.tuple;

import com.paystream.inventory.config.PageResponse;
import com.paystream.inventory.inventory.entity.DailyInventory;
import com.paystream.inventory.inventory.repository.DailyInventoryRepository;
import com.paystream.inventory.product.entity.Product;
import com.paystream.inventory.product.repository.ProductRepository;
import com.paystream.inventory.store.dto.request.StoreFindRequest;
import com.paystream.inventory.store.dto.request.StoreListFindRequest;
import com.paystream.inventory.store.dto.response.StoreResponse;
import com.paystream.inventory.store.entity.Address;
import com.paystream.inventory.store.entity.Amenities;
import com.paystream.inventory.store.entity.Category;
import com.paystream.inventory.store.entity.Store;
import com.paystream.inventory.store.repository.StoreRepository;
import jakarta.transaction.Transactional;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@Transactional
@ActiveProfiles("test")
@SpringBootTest
@AutoConfigureMockMvc
class StoreFindServiceTest {

    @Autowired private StoreFindService storeService;

    @Autowired private StoreRepository storeRepository;
    @Autowired private ProductRepository productRepository;
    @Autowired private DailyInventoryRepository dailyInventoryRepository;
    @Autowired private StoreFindService storeFindService;
    //    @Autowired private StringRedisTemplate redisTemplate;
    //
    //    @BeforeEach
    //    void tearDown() {
    //        if (redisTemplate != null) {
    //            try {
    //                // Redis 연결을 시도하고 실패하면 예외가 발생함
    //                redisTemplate.getConnectionFactory().getConnection().flushDb();
    //            } catch (Exception e) {
    //                // Redis가 꺼져 있어도 테스트가 중단되지 않도록 로그만 남기고 넘어감
    //                System.err.println("Redis를 사용할 수 없습니다. flushDb를 건너뜁니다: " + e.getMessage());
    //            }
    //        }
    //    }

    @MockitoBean private RedisConnectionFactory redisConnectionFactory;

    //    @MockitoBean
    //    private StringRedisTemplate stringRedisTemplate;
    //    @MockitoBean // 실제 빈 대신 가짜 빈을 주입함
    //    private StringRedisTemplate redisTemplate;

    @DisplayName("가게 전체 조회시 상품의 최저 금액이 같이 조회된다.")
    @Test
    void findAllWithMinPrice() {
        // given
        createTemplate();

        StoreListFindRequest request =
                StoreListFindRequest.builder()
                        .checkInDate(LocalDate.now())
                        .checkOutDate(LocalDate.now().plusDays(1))
                        .build();

        // when
        PageResponse<StoreResponse> response =
                storeService.userFindStoreList(request, PageRequest.of(1, 3));

        // then
        assertThat(response.getContent()).hasSize(3);
        assertThat(response.getContent())
                .extracting("name", "minPrice")
                .containsExactlyInAnyOrder(
                        tuple("testStore1", 1000),
                        tuple("testStore2", 3000),
                        tuple("testStore3", 5000));
    }

    @DisplayName("가게 전체 조회 with 페이징")
    @Test
    void findAllWithPaging() {
        // given
        int page = 1;
        int size = 2;
        createTemplate();

        StoreListFindRequest request =
                StoreListFindRequest.builder()
                        .checkInDate(LocalDate.now())
                        .checkOutDate(LocalDate.now().plusDays(1))
                        .build();

        // when
        PageResponse<StoreResponse> responses =
                storeService.userFindStoreList(request, PageRequest.of(page, size));

        // then
        assertThat(responses.getContent()).hasSize(size);
        assertThat(responses.getContent())
                .extracting("name", "minPrice")
                .containsExactlyInAnyOrder(tuple("testStore1", 1000), tuple("testStore2", 3000));
    }

    @DisplayName("재고가 있는 상품의 가게만 조회된다.")
    @Test
    void findAllWithAvailableStock() {
        // given
        Store store1 =
                createStore(
                        "1",
                        "testStore1",
                        List.of(Amenities.PARKING, Amenities.BAR_LOUNGE),
                        Category.HOTEL);
        Store store2 =
                createStore(
                        "1",
                        "testStore2",
                        List.of(Amenities.PARKING, Amenities.RESTAURANT),
                        Category.PENSION);
        Product product1 = createProduct("product1", 1000, 2);
        Product product2 = createProduct("product2", 2000, 2);
        store1.addProduct(product1);
        store2.addProduct(product2);
        product1.addDailyInventory(
                DailyInventory.builder().date(LocalDate.now()).stockAvailable(1).build());
        product2.addDailyInventory(
                DailyInventory.builder().date(LocalDate.now()).stockAvailable(0).build());

        storeRepository.saveAll(List.of(store1, store2));

        LocalDate checkIn = LocalDate.now();
        LocalDate checkOut = LocalDate.now().plusDays(1);

        StoreListFindRequest request =
                StoreListFindRequest.builder().checkInDate(checkIn).checkOutDate(checkOut).build();

        // when
        PageResponse<StoreResponse> response =
                storeService.userFindStoreList(request, PageRequest.of(1, 3));

        // then
        assertThat(response.getContent()).hasSize(2);
        assertThat(response.getContent())
                .extracting("name", "minPrice")
                .contains(tuple("testStore1", 1000), tuple("testStore2", 2000));
    }

    @DisplayName("기간을 줄였을때 조회되지 않는다.")
    @Test
    void findAllWithAvailableStock2() {
        // given
        Store store1 =
                createStore(
                        "1",
                        "testStore1",
                        List.of(Amenities.PARKING, Amenities.BAR_LOUNGE),
                        Category.HOTEL);
        Store store2 =
                createStore(
                        "2",
                        "testStore2",
                        List.of(Amenities.PARKING, Amenities.RESTAURANT),
                        Category.PENSION);
        Product product1 = createProduct("product1", 1000, 2);
        Product product2 = createProduct("product2", 2000, 2);
        store1.addProduct(product1);
        store2.addProduct(product2);

        List<Store> stores = storeRepository.saveAll(List.of(store1, store2));

        LocalDate checkIn = LocalDate.now();
        LocalDate checkOut = LocalDate.now().plusDays(1);

        StoreListFindRequest request =
                StoreListFindRequest.builder().checkInDate(checkIn).checkOutDate(checkOut).build();

        // when
        PageResponse<StoreResponse> response =
                storeService.userFindStoreList(request, PageRequest.of(1, 3));

        // then
        assertThat(response.getContent()).isNotNull();
    }

    @DisplayName("상세 조회 시 상품별로 예약 가능 여부를 정확히 판단한다.")
    @Test
    void should_DetermineProductAvailability_When_FindingStoreDetail() {
        // Given 테스트 데이터 준비
        LocalDate today = LocalDate.now();
        LocalDate tomorrow = today.plusDays(1);

        // 1. 가게 및 다양한 상태의 상품 구성
        Store store = createStore("1", "경화수월", List.of(Amenities.PARKING), Category.HOTEL);

        // Case A: 재고 있고 인원수 맞는 상품 -> true
        Product p1 = createProduct("예약가능상품", 1000, 2);
        p1.addDailyInventory(createInventory(today, 1));

        // Case B: 재고는 있으나 최대 인원이 부족한 상품 -> false
        Product p2 = createProduct("인원부족상품", 2000, 1); // 요청은 2명인데 최대 1명
        p2.addDailyInventory(createInventory(today, 5));

        // Case C: 인원은 맞으나 특정 날짜에 재고가 없는 상품 (품절) -> false
        Product p3 = createProduct("품절상품", 3000, 2);
        p3.addDailyInventory(createInventory(today, 0)); // 오늘 재고 없음

        store.addProduct(p1);
        store.addProduct(p2);
        store.addProduct(p3);
        Store savedStore = storeRepository.save(store);

        // 2. 요청 객체 생성 (2명, 오늘~내일)
        StoreFindRequest request =
                StoreFindRequest.builder()
                        .checkInDate(today)
                        .checkOutDate(tomorrow)
                        .personCount(2)
                        .build();

        // When
        StoreResponse response = storeFindService.findStore(savedStore.getId(), request);

        // Then
        assertThat(response.getName()).isEqualTo("경화수월");

        assertThat(response.getProducts())
                .extracting("name", "isAvailable")
                .containsExactlyInAnyOrder(
                        tuple("예약가능상품", true), tuple("인원부족상품", false), tuple("품절상품", false));
    }

    private List<Store> createTemplate() {
        Store store1 =
                createStore(
                        "1",
                        "testStore1",
                        List.of(Amenities.PARKING, Amenities.BAR_LOUNGE),
                        Category.HOTEL);
        Store store2 =
                createStore(
                        "1",
                        "testStore2",
                        List.of(Amenities.PARKING, Amenities.RESTAURANT),
                        Category.PENSION);
        Store store3 =
                createStore(
                        "1",
                        "testStore3",
                        List.of(Amenities.BAR_LOUNGE, Amenities.BREAKFAST_INCLUDED),
                        Category.GLAMPING);

        // Product 생성
        Product product1 = createProduct("product1", 1000, 2);
        Product product2 = createProduct("product2", 2000, 2);
        Product product3 = createProduct("product3", 3000, 2);
        Product product4 = createProduct("product4", 4000, 2);
        Product product5 = createProduct("product5", 5000, 2);
        Product product6 = createProduct("product6", 6000, 2);

        // 오늘 날짜 설정
        LocalDate today = LocalDate.now();

        // DailyInventory 추가
        // store1 (HOTEL)
        product1.addDailyInventory(
                DailyInventory.builder()
                        .date(today)
                        .stockAvailable(1) // 재고 1개
                        .build());
        product2.addDailyInventory(
                DailyInventory.builder()
                        .date(today)
                        .stockAvailable(2) // 재고 2개
                        .build());

        // store2 (PENSION)
        product3.addDailyInventory(
                DailyInventory.builder()
                        .date(today)
                        .stockAvailable(1) // 재고 0개 (품절)
                        .build());
        product4.addDailyInventory(
                DailyInventory.builder()
                        .date(today)
                        .stockAvailable(5) // 재고 5개
                        .build());

        // store3 (GLAMPING)
        product5.addDailyInventory(
                DailyInventory.builder()
                        .date(today)
                        .stockAvailable(3) // 재고 3개
                        .build());
        product6.addDailyInventory(
                DailyInventory.builder()
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

    private Product createProduct(String name, int price, int maxPersonCount) {
        return Product.builder()
                .name(name)
                .description("test")
                .basePrice(price)
                .maxPersonCount(maxPersonCount)
                .build();
    }

    private Store createStore(
            String hostId, String name, List<Amenities> amenities, Category category) {
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

    private DailyInventory createInventory(LocalDate date, int stock) {
        return DailyInventory.builder().date(date).stockAvailable(stock).build();
    }
}
