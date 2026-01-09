package com.paystream.inventory.store.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.paystream.core.BaseResponse;
import com.paystream.inventory.config.PageResponse;
import com.paystream.inventory.store.dto.request.StoreListFindRequest;
import com.paystream.inventory.store.dto.response.StoreResponse;
import com.paystream.inventory.store.service.StoreFindService;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

@ActiveProfiles("test")
@AutoConfigureMockMvc
@SpringBootTest
class StoreControllerTest {

    @Autowired private StoreController controller;

    @Autowired private MockMvc mockMvc;

    @Autowired private ObjectMapper objectMapper;

    @MockitoBean private StoreFindService mockService;

    @DisplayName("가게 전체 조회 Controller 단위 테스트")
    @Test
    void storeControllerWithFindAllUnitTest() throws Exception {
        // given
        StoreListFindRequest request =
                StoreListFindRequest.builder()
                        .name("한강 뷰")
                        .checkInDate(LocalDate.now())
                        .checkOutDate(LocalDate.now().plusDays(2))
                        .personCount(2)
                        .build();

        List<StoreResponse> response =
                List.of(
                        StoreResponse.builder()
                                .id(1L)
                                .name("한강 뷰 맛집")
                                .amenities(List.of("와이파이", "반려동물 동반 가능"))
                                .build());
        BaseResponse<List<StoreResponse>> result = BaseResponse.ok(response);

        Pageable pageable = PageRequest.of(0, 10);

        // page 객체로 변환
        Page<StoreResponse> pageResponse = new PageImpl<>(response, pageable, 1);
        PageResponse<StoreResponse> mockResponse = new PageResponse<>(pageResponse);
        Mockito.when(
                        mockService.userFindStoreList(
                                Mockito.any(StoreListFindRequest.class),
                                Mockito.any(Pageable.class)))
                .thenReturn(mockResponse);

        // when
        // then
        mockMvc.perform(
                        MockMvcRequestBuilders.get("/stores")
                                .param("name", request.getName())
                                .param("checkInDate", request.getCheckInDate().toString())
                                .param("checkOutDate", request.getCheckOutDate().toString())
                                .param("personCount", String.valueOf(request.getPersonCount())))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isOk())
                //                .andExpect(
                //                        MockMvcResultMatchers.content()
                //                                .json(objectMapper.writeValueAsString(result)));
                .andExpect(MockMvcResultMatchers.content().contentType("application/json"))
                .andExpect(
                        MockMvcResultMatchers.jsonPath("$.data.content[0].name")
                                .value(response.get(0).getName()));
    }
}
