package com.paystream.inventory.store.service;

import static com.paystream.inventory.store.entity.Amenities.BREAKFAST_INCLUDED;
import static com.paystream.inventory.store.entity.Amenities.PARKING;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.paystream.core.exception.PayStreamException;
import com.paystream.inventory.inventory.entity.DailyInventory;
import com.paystream.inventory.inventory.repository.DailyInventoryRepository;
import com.paystream.inventory.product.entity.Product;
import com.paystream.inventory.store.dto.request.StoreDeleteRequest;
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
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@Transactional
@ActiveProfiles("test")
@SpringBootTest
class StoreDeleteServiceTest {

    @Autowired private StoreRepository storeRepository;
    @Autowired private StoreDeleteService storeDeleteService;
    @Autowired private DailyInventoryRepository dailyInventoryRepository;

    @DisplayName("가게 삭제시 삭제해야할 가게가 없다면 오류 발생")
    @Test
    void storeDeleteByIdThrowException() {
        // given
        Store foundStore =
                createStore("1", "한강 뷰 맛집", List.of(PARKING, BREAKFAST_INCLUDED), Category.HOTEL);

        Store savedStore = storeRepository.save(foundStore);

        String hostId = "1";
        StoreDeleteRequest request =
                StoreDeleteRequest.builder().storeIds(List.of(savedStore.getId(), 2L)).build();

        // when
        // then
        assertThatThrownBy(() -> storeDeleteService.deleted(hostId, request))
                .isInstanceOf(PayStreamException.class)
                .hasMessage("삭제가 불가능한 가게가 있습니다 다시 확인해주세요.");
    }

    @DisplayName("가게를 삭제시 같이 있던 상품들도 같이 삭제된다.")
    @Test
    void deleteStoreWithProduct() {
        // given
        List<Store> storeList = createTemplate();
        List<Long> deleteStoreIds =
                storeList.stream().map(Store::getId).limit(storeList.size() - 1).toList();
        String hostId = "1";
        StoreDeleteRequest request = StoreDeleteRequest.builder().storeIds(deleteStoreIds).build();

        // when
        storeDeleteService.deleted(hostId, request);

        // then
        assertThat(storeRepository.findAll())
                .extracting(Store::getId)
                .containsExactlyInAnyOrder(storeList.get(2).getId());
    }

    @DisplayName("예약된 상품이 있다면 삭제 불가")
    @Test
    void test() {
        // given
        Store store = createStore("1", "삭제 불가능한 가게", List.of(PARKING), Category.HOTEL);

        // 재고 그대로인 상품과 깍일 상품
        Product productWithFullStock = createProduct("재고 그대로인 상품", 1000, 2);
        Product productWithReducedStock = createProduct("재고 깍일 상품", 2000, 3);

        store.addProduct(productWithFullStock);
        store.addProduct(productWithReducedStock);

        Store savedStore = storeRepository.save(store);

        // 재고 세팅
        DailyInventory fullStockInventory =
                DailyInventory.builder()
                        .product(productWithFullStock)
                        .stockAvailable(2)
                        .date(LocalDate.now())
                        .build();
        DailyInventory reducedStockInventory =
                DailyInventory.builder()
                        .product(productWithReducedStock)
                        .stockAvailable(3)
                        .date(LocalDate.now())
                        .build();

        List<DailyInventory> dailyInventories =
                dailyInventoryRepository.saveAll(
                        List.of(fullStockInventory, reducedStockInventory));
        DailyInventory reduceStockInventoryExtract = dailyInventories.get(1); // 깍을 재고 추출
        reduceStockInventoryExtract.decreaseStockAvailable(); // 재고 감소

        StoreDeleteRequest request =
                StoreDeleteRequest.builder().storeIds(List.of(savedStore.getId())).build();

        // when
        // then
        assertThatThrownBy(() -> storeDeleteService.deleted("1", request))
                .isInstanceOf(PayStreamException.class)
                .hasMessage("삭제가 불가능한 가게가 있습니다 다시 확인해주세요.");
    }

    private Store createStore(
            String hostId, String name, List<Amenities> amenities, Category category) {
        return Store.builder()
                .hostId(hostId)
                .name(name)
                .category(category)
                .amenities(amenities)
                .checkInTime(LocalTime.of(15, 0))
                .checkOutTime(LocalTime.of(11, 0))
                .build();
    }

    private Product createProduct(String name, int price, int baseStock) {
        return Product.builder()
                .name(name)
                .description("test")
                .basePrice(price)
                .baseStock(baseStock)
                .build();
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
        product1.addDailyInventory(DailyInventory.builder().date(today).stockAvailable(2).build());
        product2.addDailyInventory(DailyInventory.builder().date(today).stockAvailable(2).build());

        // store2 (PENSION)
        product3.addDailyInventory(DailyInventory.builder().date(today).stockAvailable(2).build());
        product4.addDailyInventory(DailyInventory.builder().date(today).stockAvailable(2).build());

        // store3 (GLAMPING)
        product5.addDailyInventory(DailyInventory.builder().date(today).stockAvailable(2).build());
        product6.addDailyInventory(DailyInventory.builder().date(today).stockAvailable(2).build());

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
