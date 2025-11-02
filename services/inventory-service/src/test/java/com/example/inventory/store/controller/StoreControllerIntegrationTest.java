package com.example.inventory.store.controller;

import com.example.core.BaseResponse;
import com.example.inventory.inventory.entity.DailyInventory;
import com.example.inventory.product.entity.Product;
import com.example.inventory.store.dto.request.StoreUserFindRequest;
import com.example.inventory.store.dto.response.StoreResponse;
import com.example.inventory.store.entity.Amenities;
import com.example.inventory.store.entity.Category;
import com.example.inventory.store.entity.Store;
import com.example.inventory.store.repository.StoreQueryDslRepository;
import com.example.inventory.store.repository.StoreRepository;
import com.example.inventory.store.service.StoreFindService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import static com.example.inventory.store.entity.Amenities.*;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

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
    private StoreFindService storeService;

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
        Store foundStore2 = createStore(
                "2", "강남 뷰 상품", List.of(PARKING, BREAKFAST_INCLUDED), Category.HOTEL);
        Product foundProduct2 = createProduct("강남 뷰 상품", 30000);

        foundProduct.addDailyInventory(DailyInventory.builder().date(today).stockAvailable(1).build());
        foundProduct.addDailyInventory(DailyInventory.builder().date(today.plusDays(1)).stockAvailable(1).build());
        foundProduct.addDailyInventory(DailyInventory.builder().date(today.plusDays(2)).stockAvailable(1).build());
        foundStore.addProduct(foundProduct);

        foundProduct2.addDailyInventory(DailyInventory.builder().date(today).stockAvailable(2).build());
        foundProduct2.addDailyInventory(DailyInventory.builder().date(today.plusDays(1)).stockAvailable(2).build());
        foundProduct2.addDailyInventory(DailyInventory.builder().date(today.plusDays(2)).stockAvailable(2).build());
        foundStore2.addProduct(foundProduct2);

        Store notFoundStore = createStore(
                "2", "남산 뷰 펜션", List.of(RESTAURANT), Category.PENSION);
        notFoundStore.addProduct(createProduct("남산 상품", 10000));

        List<Store> savedStores = storeRepository.saveAll(List.of(foundStore, foundStore2, notFoundStore));

        StoreUserFindRequest request = StoreUserFindRequest.builder()
//                .name("한강 뷰")
                .checkIn(today)
                .checkOut(today.plusDays(2))
                .build();

        List<StoreResponse> expectedResponse = List.of(
                StoreResponse.of(foundStore, 25000),
                StoreResponse.of(foundStore2, 30000)
        );
        BaseResponse<List<StoreResponse>> result = BaseResponse.ok(expectedResponse);

        // when
        // then
        mockMvc.perform(
                        get("/stores")
                                .param("name", request.getName())
                                .param("checkIn", request.getCheckIn().toString())
                                .param("checkOut", request.getCheckOut().toString())
                )
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(APPLICATION_JSON))
                .andExpect(jsonPath("$.code").value("200"))
                .andExpect(jsonPath("$.message").value("OK"))
                .andExpect(content().json(objectMapper.writeValueAsString(result)));
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
