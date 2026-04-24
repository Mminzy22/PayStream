package com.paystream.notification.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

/** HealthController 단위 테스트 */
@WebMvcTest(HealthController.class)
class HealthControllerTests {

    @Autowired private MockMvc mockMvc;

    @MockitoBean private JpaMetamodelMappingContext jpaMetamodelMappingContext;

    @Test
    @DisplayName("ping 엔드포인트 - 정상 응답 확인")
    void testPingEndpoint() throws Exception {
        mockMvc.perform(get("/notifications/ping"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.service").value("notification-service"))
                .andExpect(jsonPath("$.status").value("UP"))
                .andExpect(jsonPath("$.message").exists())
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    @DisplayName("info 엔드포인트 - 서비스 정보 조회")
    void testInfoEndpoint() throws Exception {
        mockMvc.perform(get("/notifications/info"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("PayStream Notification Service"))
                .andExpect(jsonPath("$.version").value("0.0.1-SNAPSHOT"))
                .andExpect(jsonPath("$.description").exists());
    }
}
