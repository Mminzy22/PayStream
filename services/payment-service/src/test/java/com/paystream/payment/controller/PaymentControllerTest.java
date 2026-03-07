package com.paystream.payment.controller;

import com.paystream.payment.service.PaymentService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
@TestPropertySource(
        properties = {
            "eureka.client.enabled=false",
            "eureka.client.register-with-eureka=false",
            "eureka.client.fetch-registry=false",
            "spring.datasource.url=jdbc:h2:mem:testdb",
            "spring.datasource.driver-class-name=org.h2.Driver",
            "spring.jpa.database-platform=org.hibernate.dialect.H2Dialect",
            "spring.jpa.hibernate.ddl-auto=create-drop",
            "portone.store-id=test-store-id",
            "portone.channel-key=test-channel-key",
            "portone.secret-key=test-secret-key",
            "portone.webhook-secret=test-webhook-secret"
        })
class PaymentControllerTest {

    @Autowired private MockMvc mockMvc;

    @MockitoBean private PaymentService paymentService;

    @Test
    void ping_shouldReturnOk() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/payments/ping"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(
                        MockMvcResultMatchers.jsonPath("$.data.service").value("payment-service"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.data.status").value("UP"));
    }
}
