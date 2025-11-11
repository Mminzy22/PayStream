package com.example.inventory.store.controller;

import com.example.core.BaseResponse;
import com.example.inventory.store.dto.request.StoreListFindRequest;
import com.example.inventory.store.dto.response.StoreResponse;
import com.example.inventory.store.service.StoreFindService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.time.LocalDate;
import java.util.List;

import static com.example.inventory.store.entity.Amenities.BREAKFAST_INCLUDED;
import static com.example.inventory.store.entity.Amenities.PARKING;

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
    private StoreFindService mockService;

    @DisplayName("가게 전체 조회 Controller 단위 테스트")
    @Test
    void storeControllerWithFindAllUnitTest() throws Exception {
        // given
        StoreListFindRequest request = StoreListFindRequest.builder()
                .name("한강 뷰")
                .checkInDate(LocalDate.now())
                .checkOutDate(LocalDate.now().plusDays(2))
                .personCount(2)
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
                Mockito.any(StoreListFindRequest.class),
                Mockito.any(Pageable.class)
        )).thenReturn(mockResponse);

        // when
        // then
        mockMvc.perform(
                        MockMvcRequestBuilders.get("/stores")
                                .param("name", request.getName())
                                .param("checkInDate", request.getCheckInDate().toString())
                                .param("checkOutDate", request.getCheckOutDate().toString())
                                .param("personCount", String.valueOf(request.getPersonCount()))
        )
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().json(objectMapper.writeValueAsString(result)));

    }



}