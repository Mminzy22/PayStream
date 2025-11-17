package com.paystream.notification.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.test.web.servlet.MockMvc;

/** HealthController 단위 테스트 */
@WebMvcTest(HealthController.class)
class HealthControllerTests {

    @Autowired private MockMvc mockMvc;

    // JPA Auditing에서 요구하는 매핑 컨텍스트를 목으로 등록해 WebMvcTest에서 DB 의존성을 제거
    @MockBean
    private JpaMetamodelMappingContext jpaMetamodelMappingContext;

    @Test
    @DisplayName("ping 엔드포인트 - 정상 응답 확인")
    void testPingEndpoint() throws Exception {
        mockMvc.perform(get("/api/notifications/ping"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.service").value("notification-service"))
                .andExpect(jsonPath("$.status").value("UP"))
                .andExpect(jsonPath("$.message").exists())
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    @DisplayName("info 엔드포인트 - 서비스 정보 조회")
    void testInfoEndpoint() throws Exception {
        mockMvc.perform(get("/api/notifications/info"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("PayStream Notification Service"))
                .andExpect(jsonPath("$.version").value("0.0.1-SNAPSHOT"))
                .andExpect(jsonPath("$.description").exists());
    }
}
