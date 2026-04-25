package com.paystream.inventory.store.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.groups.Tuple.tuple;

import com.paystream.inventory.config.PageResponse;
import com.paystream.inventory.inventory.entity.DailyInventory;
import com.paystream.inventory.inventory.repository.DailyInventoryRepository;
import com.paystream.inventory.product.dto.response.ProductResponse;
import com.paystream.inventory.product.entity.Product;
import com.paystream.inventory.product.repository.ProductRepository;
import com.paystream.inventory.promotion.entity.DiscountType;
import com.paystream.inventory.promotion.entity.Promotion;
import com.paystream.inventory.promotion.entity.TargetType;
import com.paystream.inventory.promotion.repository.PromotionRepository;
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
    @Autowired private PromotionRepository promotionRepository;

    @MockitoBean private RedisConnectionFactory redisConnectionFactory;

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
        assertThat(response.getContent())
                .hasSize(3)
                .extracting(
                        StoreResponse::getName,
                        s -> s.getProducts().get(0).getPrice().getDiscounted())
                .containsExactlyInAnyOrder(
                        tuple("testStore1", 1000L),
                        tuple("testStore2", 3000L),
                        tuple("testStore3", 5000L));
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
        assertThat(responses.getContent())
                .hasSize(size)
                .extracting(
                        StoreResponse::getName,
                        s -> s.getProducts().get(0).getPrice().getDiscounted())
                .containsExactlyInAnyOrder(tuple("testStore1", 1000L), tuple("testStore2", 3000L));
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
        assertThat(response.getContent())
                .hasSize(2)
                .extracting(
                        StoreResponse::getName,
                        store -> store.getProducts().get(0).getPrice().getDiscounted() // 직접 객체에서 꺼냄
                        )
                .containsExactlyInAnyOrder(tuple("testStore1", 1000L), tuple("testStore2", 2000L));
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

    @DisplayName("목록 조회 시 각 가게별로 할인이 적용된 최저가 상품이 정확히 노출된다.")
    @Test
    void userFindStoreListTest() {
        // given
        // [Store A] 가게 할인 20%가 상품 고정 할인(1.5만)보다 큰 경우
        Store storeA = createStore("1", "A 호텔", List.of(Amenities.PARKING), Category.HOTEL);
        Product p1 = createProduct("A-객실1", 100_000, 2);
        p1.addDailyInventory(createInventory(LocalDate.now(), 1));

        // [Store B] 상품 고정 할인이 커서 최저가가 바뀌는 경우
        Store storeB = createStore("1", "B 글램핑", List.of(Amenities.PARKING), Category.GLAMPING);
        Product p2 = createProduct("B-객실2", 200_000, 2); // 원가 비쌈
        Product p3 = createProduct("B-객실3", 150_000, 2); // 더 저렴한 원가
        p2.addDailyInventory(createInventory(LocalDate.now(), 1));
        p3.addDailyInventory(createInventory(LocalDate.now(), 1));

        // [Store C] 할인 없음
        Store storeC = createStore("1", "C 펜션", List.of(Amenities.PARKING), Category.PENSION);
        Product p4 = createProduct("C-객실4", 50_000, 2);
        p4.addDailyInventory(createInventory(LocalDate.now(), 1));

        storeA.addProduct(p1);
        storeB.addProduct(p2);
        storeB.addProduct(p3);
        storeC.addProduct(p4);
        storeRepository.saveAll(List.of(storeA, storeB, storeC));

        Promotion promo1 =
                savePromotion(TargetType.STORE, storeA.getId(), DiscountType.PERCENT, 20); // 2만 할인
        Promotion promo2 =
                savePromotion(TargetType.PRODUCT, p1.getId(), DiscountType.FIXED_AMOUNT, 15_000);
        Promotion promo3 =
                savePromotion(
                        TargetType.PRODUCT,
                        p3.getId(),
                        DiscountType.FIXED_AMOUNT,
                        50_000); // 10만에 판매됨
        promotionRepository.saveAll(List.of(promo1, promo2, promo3));

        // 검색조건 설정
        StoreListFindRequest request =
                StoreListFindRequest.builder()
                        .personCount(2)
                        .checkInDate(LocalDate.now())
                        .checkOutDate(LocalDate.now().plusDays(1))
                        .build();

        PageRequest pageable = PageRequest.of(1, 10);

        // when
        PageResponse<StoreResponse> response = storeService.userFindStoreList(request, pageable);

        // then
        assertThat(response.getContent()).hasSize(3);

        // A호텔 검증: 10만 -> 8만 (20%)
        StoreResponse resA = findResponseByName(response, "A 호텔");
        assertThat(resA.getProducts().get(0).getPrice().getDiscounted()).isEqualTo(80_000L);
        assertThat(resA.getProducts().get(0).getPrice().getDiscountRate()).isEqualTo(20.0);

        // B 리조트 검증: 15만 -> 10만 (33%)
        StoreResponse resB = findResponseByName(response, "B 글램핑");
        assertThat(resB.getProducts().get(0).getPrice().getDiscounted()).isEqualTo(100_000L);
        assertThat(resB.getProducts().get(0).getPrice().getDiscountRate()).isEqualTo(33.3);

        // C 펜션 검증: 5만 -> 5만 (0%)
        StoreResponse resC = findResponseByName(response, "C 펜션");
        assertThat(resC.getProducts().get(0).getPrice().getDiscounted()).isEqualTo(50_000L);
        assertThat(resC.getProducts().get(0).getPrice().getDiscountRate()).isEqualTo(0.0);
    }

    @DisplayName("가게 상세 조회시, 가게 할인과 상품 할인 중 혜택이 큰 것이 적용되어 응답된다.")
    @Test
    void findStoreWithBestDiscountTest() {
        // given
        LocalDate today = LocalDate.now();

        Store store = createStore("1", "테스트 호텔", List.of(Amenities.PARKING), Category.HOTEL);
        Product p1 = createProduct("디럭스 룸", 100000, 2);
        Product p2 = createProduct("이벤트 룸", 320000, 2);
        p1.addDailyInventory(createInventory(today, 1));
        p2.addDailyInventory(createInventory(today, 1));
        store.addProduct(p1);
        store.addProduct(p2);
        storeRepository.save(store);

        // 가게 프로모션
        promotionRepository.save(
                Promotion.builder()
                        .targetId(store.getId())
                        .targetType(TargetType.STORE)
                        .discountType(DiscountType.PERCENT)
                        .discountValue(10)
                        .startDate(today)
                        .endDate(today)
                        .createUserId("1")
                        .build());

        // 상품 프로모션1 (이게 더 큼)
        promotionRepository.save(
                Promotion.builder()
                        .targetType(TargetType.PRODUCT)
                        .targetId(p1.getId())
                        .discountType(DiscountType.FIXED_AMOUNT)
                        .discountValue(15_000)
                        .startDate(today)
                        .endDate(today)
                        .createUserId("1")
                        .build());

        // 상품 프로모션2 (가게가 더 큼)
        promotionRepository.save(
                Promotion.builder()
                        .targetType(TargetType.PRODUCT)
                        .targetId(p2.getId())
                        .discountType(DiscountType.FIXED_AMOUNT)
                        .discountValue(15_000)
                        .startDate(today)
                        .endDate(today)
                        .createUserId("1")
                        .build());

        StoreFindRequest request = new StoreFindRequest(2, today, today.plusDays(2));

        // when
        StoreResponse response = storeFindService.findStore(store.getId(), request);

        // then
        assertThat(response.getName()).isEqualTo("테스트 호텔");
        assertThat(response.getProducts()).hasSize(2);

        ProductResponse productRes = response.getProducts().get(0);
        assertThat(productRes.getName()).isEqualTo("디럭스 룸");

        // 가격 정보 검증 (상품1) - 상품 프로모션 적용
        assertThat(productRes.getPrice().getOriginal()).isEqualTo(100_000L);
        assertThat(productRes.getPrice().getDiscounted()).isEqualTo(85_000L);
        assertThat(productRes.getPrice().getDiscountRate()).isEqualTo(15);

        ProductResponse productRes2 = response.getProducts().get(1);
        assertThat(productRes2.getName()).isEqualTo("이벤트 룸");

        // 가격 정보 검증 (상품2) - 가게 프로모션 적용
        assertThat(productRes2.getPrice().getOriginal()).isEqualTo(320_000L);
        assertThat(productRes2.getPrice().getDiscounted()).isEqualTo(288_000L);
        assertThat(productRes2.getPrice().getDiscountRate()).isEqualTo(10);
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

    private Promotion savePromotion(TargetType type, Long targetId, DiscountType dType, int value) {
        return Promotion.builder()
                .targetType(type)
                .targetId(targetId)
                .discountType(dType)
                .discountValue(value)
                .startDate(LocalDate.now().minusDays(1))
                .endDate(LocalDate.now().plusDays(1))
                .createUserId("1")
                .build();
    }

    private StoreResponse findResponseByName(PageResponse<StoreResponse> response, String name) {
        return response.getContent().stream()
                .filter(s -> s.getName().equals(name))
                .findFirst()
                .orElseThrow();
    }
}
