package com.paystream.notification.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.paystream.notification.domain.NotificationChannel;
import com.paystream.notification.domain.NotificationStatus;
import com.paystream.notification.dto.NotificationRequest;
import com.paystream.notification.dto.NotificationResponse;
import com.paystream.notification.dto.NotificationUpdateRequest;
import com.paystream.notification.service.NotificationCreateService;
import com.paystream.notification.service.NotificationFindService;
import com.paystream.notification.service.NotificationUpdateService;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

/** NotificationController 단위 테스트 */
@ActiveProfiles("test")
@AutoConfigureMockMvc(addFilters = false)
@SpringBootTest
@TestPropertySource(
        properties = {
            "eureka.client.enabled=false",
            "eureka.client.register-with-eureka=false",
            "eureka.client.fetch-registry=false"
        })
class NotificationControllerTest {

    @Autowired private MockMvc mockMvc;

    @Autowired private ObjectMapper objectMapper;

    @MockitoBean private NotificationCreateService notificationCreateService;

    @MockitoBean private NotificationFindService notificationFindService;

    @MockitoBean private NotificationUpdateService notificationUpdateService;

    @MockitoBean private JpaMetamodelMappingContext jpaMetamodelMappingContext;

    @Test
    @DisplayName("알림 생성 - 정상 요청")
    void createShouldReturnCreated() throws Exception {
        // given
        Long notificationId = 1L;
        NotificationRequest request =
                NotificationRequest.builder()
                        .userId(1L)
                        .channel(NotificationChannel.EMAIL)
                        .title("테스트 알림")
                        .body("테스트 알림 본문")
                        .build();

        when(notificationCreateService.create(any(NotificationRequest.class)))
                .thenReturn(notificationId);

        // when & then
        mockMvc.perform(
                        post("/api/notifications")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data").value(notificationId));
    }

    @Test
    @DisplayName("알림 생성 - 필수 필드 누락 시 400 에러")
    void createShouldReturnBadRequestWhenRequiredFieldMissing() throws Exception {
        // given
        NotificationRequest request =
                NotificationRequest.builder()
                        .userId(1L)
                        // channel 누락
                        .title("테스트 알림")
                        .body("테스트 알림 본문")
                        .build();

        // when & then
        mockMvc.perform(
                        post("/api/notifications")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("알림 조회 - ID로 조회 성공")
    void findByIdShouldReturnOk() throws Exception {
        // given
        Long notificationId = 1L;
        NotificationResponse response =
                NotificationResponse.builder()
                        .id(notificationId)
                        .userId(1L)
                        .channel(NotificationChannel.EMAIL)
                        .title("테스트 알림")
                        .body("테스트 알림 본문")
                        .status(NotificationStatus.PENDING)
                        .build();

        when(notificationFindService.findById(notificationId)).thenReturn(response);

        // when & then
        mockMvc.perform(get("/api/notifications/{id}", notificationId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(notificationId))
                .andExpect(jsonPath("$.data.userId").value(1L))
                .andExpect(jsonPath("$.data.channel").value("EMAIL"))
                .andExpect(jsonPath("$.data.title").value("테스트 알림"))
                .andExpect(jsonPath("$.data.status").value("PENDING"));
    }

    @Test
    @DisplayName("사용자별 알림 목록 조회 - 성공")
    void findByUserIdShouldReturnOk() throws Exception {
        // given
        Long userId = 1L;
        NotificationResponse response1 =
                NotificationResponse.builder()
                        .id(1L)
                        .userId(userId)
                        .channel(NotificationChannel.EMAIL)
                        .title("알림 1")
                        .status(NotificationStatus.SENT)
                        .build();

        NotificationResponse response2 =
                NotificationResponse.builder()
                        .id(2L)
                        .userId(userId)
                        .channel(NotificationChannel.EMAIL)
                        .title("알림 2")
                        .status(NotificationStatus.PENDING)
                        .build();

        List<NotificationResponse> responses = Arrays.asList(response1, response2);

        when(notificationFindService.findByUserId(userId)).thenReturn(responses);

        // when & then
        mockMvc.perform(get("/api/notifications/users/{userId}", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(2))
                .andExpect(jsonPath("$.data[0].id").value(1L))
                .andExpect(jsonPath("$.data[1].id").value(2L));
    }

    @Test
    @DisplayName("알림 상태 업데이트 - 성공")
    void updateShouldReturnOk() throws Exception {
        // given
        Long notificationId = 1L;
        NotificationUpdateRequest request =
                NotificationUpdateRequest.builder().status(NotificationStatus.SENT).build();

        NotificationResponse response =
                NotificationResponse.builder()
                        .id(notificationId)
                        .userId(1L)
                        .channel(NotificationChannel.EMAIL)
                        .title("테스트 알림")
                        .status(NotificationStatus.SENT)
                        .build();

        when(notificationUpdateService.update(anyLong(), any(NotificationUpdateRequest.class)))
                .thenReturn(response);

        // when & then
        mockMvc.perform(
                        put("/api/notifications/{id}", notificationId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(notificationId))
                .andExpect(jsonPath("$.data.status").value("SENT"));
    }
}
