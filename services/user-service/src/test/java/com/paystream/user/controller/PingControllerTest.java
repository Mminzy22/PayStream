package com.paystream.user.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

@WebMvcTest(
        controllers = PingController.class,
        excludeAutoConfiguration = {
            org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration.class
        })
@TestPropertySource(
        properties = {
            "eureka.client.enabled=false",
            "eureka.client.register-with-eureka=false",
            "eureka.client.fetch-registry=false"
        })
class PingControllerTest {

    @Autowired private MockMvc mockMvc;

    @Test
    void ping_shouldReturnOk() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/users/ping"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.service").value("user-service"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.status").value("UP"));
    }
}
