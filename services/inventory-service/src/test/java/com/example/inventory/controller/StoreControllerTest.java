package com.example.inventory.controller;

import com.example.core.BaseResponse;
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
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
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

@ActiveProfiles("test")
@AutoConfigureMockMvc
@SpringBootTest
class StoreControllerTest {

    @Autowired
    private StoreController controller;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private StoreService mockService;

    @DisplayName("가게 전체 조회 Controller 단위 테스트")
    @Test
    void storeControllerWithFindAllUnitTest() throws Exception {
        // given
        StoreUserFindRequest request = StoreUserFindRequest.builder()
                .name("한강 뷰")
                .checkIn(LocalDate.now())
                .checkOut(LocalDate.now().plusDays(2))
                .build();

        List<StoreResponse> mockResponse = List.of(
                StoreResponse.builder()
                        .id(1L)
                        .name("한강 뷰 맛집")
                        .amenities(List.of(PARKING, BREAKFAST_INCLUDED))
                        .minPrice(25000)
                        .build()
        );
        BaseResponse<List<StoreResponse>> result = BaseResponse.ok(mockResponse);

        Pageable pageable = PageRequest.of(0, 10);

        Mockito.when(mockService.userFindStoreList(
                Mockito.any(StoreUserFindRequest.class),
                Mockito.any(Pageable.class)
        )).thenReturn(mockResponse);

        // when
        // then
        mockMvc.perform(
                        MockMvcRequestBuilders.get("/stores")
                                .param("name", request.getName())
                                .param("checkIn", request.getCheckIn().toString())
                                .param("checkOut", request.getCheckOut().toString())
        )
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().json(objectMapper.writeValueAsString(result)));

    }



}