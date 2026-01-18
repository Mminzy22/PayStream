package com.paystream.payment.controller;

import com.paystream.payment.service.PaymentService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

@WebMvcTest(PaymentController.class)
@TestPropertySource(
        properties = {
            "eureka.client.enabled=false",
            "eureka.client.register-with-eureka=false",
            "eureka.client.fetch-registry=false"
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
