package com.example.inventory.controller;

import com.example.inventory.dto.store.StoreResponse;
import com.example.inventory.dto.store.request.StoreUserFindRequest;
import com.example.inventory.entity.inventory.DailyInventory;
import com.example.inventory.entity.product.Product;
import com.example.inventory.entity.store.Amenities;
import com.example.inventory.entity.store.Category;
import com.example.inventory.entity.store.Store;
import com.example.inventory.repository.StoreQueryDslRepository;
import com.example.inventory.repository.StoreRepository;
import com.example.inventory.service.StoreService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import static com.example.inventory.entity.store.Amenities.*;
import static com.example.inventory.entity.store.Amenities.BREAKFAST_INCLUDED;
import static com.example.inventory.entity.store.Amenities.PARKING;

@ActiveProfiles("test")
@AutoConfigureMockMvc
@SpringBootTest
public class StoreControllerIntegrationTest {

    @Autowired
    private StoreController controller;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private StoreService storeService;

    @Autowired
    private StoreRepository storeRepository;

    @Autowired
    private StoreQueryDslRepository storeQueryDslRepository;

    @Transactional
    @DisplayName("가게 전체 조회 Controller 통합 테스트")
    @Test
    void storeControllerWithFindAllIntegrationTest() throws Exception {
        // given
        LocalDate today = LocalDate.now();

        Store foundStore = createStore(
                "1", "한강 뷰 맛집", List.of(PARKING, BREAKFAST_INCLUDED), Category.HOTEL);
        Product foundProduct = createProduct("한강 뷰 상품", 25000);

        foundProduct.addDailyInventory(DailyInventory.builder().date(today).stockAvailable(1).build());
        foundProduct.addDailyInventory(DailyInventory.builder().date(today.plusDays(1)).stockAvailable(1).build());
        foundProduct.addDailyInventory(DailyInventory.builder().date(today.plusDays(2)).stockAvailable(1).build());
        foundStore.addProduct(foundProduct);

        Store notFoundStore = createStore(
                "2", "남산 뷰 펜션", List.of(RESTAURANT), Category.PENSION);
        notFoundStore.addProduct(createProduct("남산 상품", 10000));

        List<Store> savedStores = storeRepository.saveAll(List.of(foundStore, notFoundStore));

        StoreUserFindRequest request = StoreUserFindRequest.builder()
                .name("한강 뷰")
                .checkIn(today)
                .checkOut(today.plusDays(2))
                .build();

        List<StoreResponse> expectedResponse = List.of(
                StoreResponse.builder()
                        .id(foundStore.getId())
                        .hostId(foundStore.getHostId())
                        .name(foundStore.getName())
                        .description(foundStore.getDescription())
                        .address(foundStore.getAddress())
                        .category(foundStore.getCategory())
                        .checkInTime(foundStore.getCheckInTime())
                        .checkOutTime(foundStore.getCheckOutTime())
                        .rating(foundStore.getRating())
                        .reviewCount(foundStore.getReviewCount())
                        .amenities(List.of(PARKING, BREAKFAST_INCLUDED))
                        .minPrice(25000)
                        .build()
        );

        // when
        // then
        mockMvc.perform(
                        MockMvcRequestBuilders.get("/stores")
                                .queryParam("name", request.getName())
                                .param("checkIn", request.getCheckIn().toString())
                                .param("checkOut", request.getCheckOut().toString())
                )
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().json(objectMapper.writeValueAsString(expectedResponse)));
    }

    private Product createProduct(String name, int price) {
        return Product.builder().name(name).basePrice(price).build();
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

}
