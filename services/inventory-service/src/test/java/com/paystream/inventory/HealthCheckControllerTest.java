package com.paystream.inventory;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

@ActiveProfiles("test")
@WebMvcTest(HealthCheckController.class)
@AutoConfigureMockMvc(addFilters = false) // Spring Security 자동 구성 비활성화
class HealthCheckControllerTest {

    @Autowired private MockMvc mockMvc;

    @DisplayName("actuator health 체크")
    @Test
    void application_heath_check() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/inventories/ping"))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.service").value("inventory-service"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.status").value("UP"));
    }
}
