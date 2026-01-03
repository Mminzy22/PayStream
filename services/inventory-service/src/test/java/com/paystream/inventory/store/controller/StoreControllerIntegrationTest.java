package com.paystream.inventory.store.controller;

import static com.paystream.inventory.store.entity.Amenities.*;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.paystream.core.BaseResponse;
import com.paystream.inventory.config.PageResponse;
import com.paystream.inventory.inventory.entity.DailyInventory;
import com.paystream.inventory.product.entity.Product;
import com.paystream.inventory.store.dto.request.StoreCreateRequest;
import com.paystream.inventory.store.dto.request.StoreListFindRequest;
import com.paystream.inventory.store.dto.response.StoreResponse;
import com.paystream.inventory.store.entity.Address;
import com.paystream.inventory.store.entity.Amenities;
import com.paystream.inventory.store.entity.Category;
import com.paystream.inventory.store.entity.Store;
import com.paystream.inventory.store.repository.StoreQueryDslRepository;
import com.paystream.inventory.store.repository.StoreRepository;
import com.paystream.inventory.store.service.StoreFindService;
import jakarta.transaction.Transactional;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

@ActiveProfiles("test")
@AutoConfigureMockMvc
@SpringBootTest
public class StoreControllerIntegrationTest {

    @Autowired private StoreController controller;

    @Autowired private ObjectMapper objectMapper;

    @Autowired private MockMvc mockMvc;

    @Autowired private StoreFindService storeService;

    @Autowired private StoreRepository storeRepository;

    @Autowired private StoreQueryDslRepository storeQueryDslRepository;

    @Transactional
    @DisplayName("가게 전체 조회 Controller 통합 테스트")
    @Test
    void storeControllerWithFindAllIntegrationTest() throws Exception {
        // given
        LocalDate today = LocalDate.now();

        Store foundStore =
                createStore("1", "한강 뷰 맛집", List.of(PARKING, BREAKFAST_INCLUDED), Category.HOTEL);
        Product foundProduct = createProduct("한강 뷰 상품", 25000, 2);
        Store foundStore2 =
                createStore("2", "강남 뷰 상품", List.of(PARKING, BREAKFAST_INCLUDED), Category.HOTEL);
        Product foundProduct2 = createProduct("강남 뷰 상품", 30000, 3);

        foundProduct.addDailyInventory(
                DailyInventory.builder().date(today).stockAvailable(1).build());
        foundProduct.addDailyInventory(
                DailyInventory.builder().date(today.plusDays(1)).stockAvailable(1).build());
        foundProduct.addDailyInventory(
                DailyInventory.builder().date(today.plusDays(2)).stockAvailable(1).build());
        foundStore.addProduct(foundProduct);

        foundProduct2.addDailyInventory(
                DailyInventory.builder().date(today).stockAvailable(2).build());
        foundProduct2.addDailyInventory(
                DailyInventory.builder().date(today.plusDays(1)).stockAvailable(2).build());
        foundProduct2.addDailyInventory(
                DailyInventory.builder().date(today.plusDays(2)).stockAvailable(2).build());
        foundStore2.addProduct(foundProduct2);

        Store notFoundStore = createStore("2", "남산 뷰 펜션", List.of(RESTAURANT), Category.PENSION);
        notFoundStore.addProduct(createProduct("남산 상품", 10000, 2));

        List<Store> savedStores =
                storeRepository.saveAll(List.of(foundStore, foundStore2, notFoundStore));

        StoreListFindRequest request =
                StoreListFindRequest.builder()
                        //                .name("한강 뷰")
                        .checkInDate(today)
                        .checkOutDate(today.plusDays(2))
                        .personCount(2)
                        .build();

        List<StoreResponse> expectedResponse =
                List.of(StoreResponse.of(foundStore, 25000), StoreResponse.of(foundStore2, 30000));
        Page<StoreResponse> pageResponse =
                new PageImpl<>(expectedResponse, PageRequest.of(0, 10), expectedResponse.size());

        BaseResponse<PageResponse<StoreResponse>> result =
                BaseResponse.ok(new PageResponse<>(pageResponse));

        // when
        // then
        mockMvc.perform(
                        get("/stores")
                                .param("name", request.getName())
                                .param("checkInDate", request.getCheckInDate().toString())
                                .param("checkOutDate", request.getCheckOutDate().toString())
                                .param("personCount", String.valueOf(request.getPersonCount())))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(APPLICATION_JSON))
                .andExpect(jsonPath("$.code").value("200"))
                .andExpect(jsonPath("$.message").value("OK"))
                .andExpect(content().json(objectMapper.writeValueAsString(result)));
    }

    @DisplayName("가게 생성")
    @Test
    void createStoreTest() throws Exception {
        // given
        StoreCreateRequest request =
                StoreCreateRequest.builder()
                        .hostId("1")
                        .name("한강 뷰 호텔")
                        .address(new Address("서울시", "여의도"))
                        .category(Category.HOTEL)
                        .checkInTime(LocalTime.now())
                        .checkOutTime(LocalTime.now())
                        .amenities(List.of(BAR_LOUNGE, PARKING))
                        .basePersonCount(2)
                        .rule("")
                        .build();

        // when
        // then
        mockMvc.perform(
                        post("/stores")
                                .contentType(APPLICATION_JSON)
                                .content(objectMapper.writeValueAsBytes(request)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(APPLICATION_JSON))
                .andExpect(jsonPath("$.code").value("201"))
                .andExpect(jsonPath("$.message").value("CREATED"))
                .andExpect(jsonPath("$.data").value("1"));
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
                .category(category)
                .amenities(amenities)
                .checkInTime(LocalTime.of(15, 0))
                .checkOutTime(LocalTime.of(11, 0))
                .build();
    }
}
