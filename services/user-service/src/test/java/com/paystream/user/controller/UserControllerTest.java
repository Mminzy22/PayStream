package com.paystream.user.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.paystream.user.dto.request.UserUpdateRequest;
import com.paystream.user.dto.response.UserResponse;
import com.paystream.user.service.UserDeleteService;
import com.paystream.user.service.UserFindService;
import com.paystream.user.service.UserUpdateService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(
        controllers = UserController.class,
        excludeAutoConfiguration = {
            org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration.class
        })
@TestPropertySource(
        properties = {
            "eureka.client.enabled=false",
            "eureka.client.register-with-eureka=false",
            "eureka.client.fetch-registry=false"
        })
class UserControllerTest {

    @Autowired private MockMvc mockMvc;

    @Autowired private ObjectMapper objectMapper;

    @MockBean private UserFindService userFindService;

    @MockBean private UserUpdateService userUpdateService;

    @MockBean private UserDeleteService userDeleteService;

    @Test
    void findById_shouldReturnOk() throws Exception {
        // given
        Long userId = 1L;
        UserResponse userResponse =
                UserResponse.builder().id(userId).email("test@example.com").name("테스트 사용자").build();

        when(userFindService.findById(userId)).thenReturn(userResponse);

        // when & then
        mockMvc.perform(get("/users/{id}", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(userId))
                .andExpect(jsonPath("$.data.email").value("test@example.com"))
                .andExpect(jsonPath("$.data.name").value("테스트 사용자"));
    }

    @Test
    void update_shouldReturnOk() throws Exception {
        // given
        Long userId = 1L;
        UserUpdateRequest request =
                UserUpdateRequest.builder().name("수정된 이름").phone("010-1234-5678").build();

        UserResponse userResponse =
                UserResponse.builder()
                        .id(userId)
                        .email("test@example.com")
                        .name("수정된 이름")
                        .phone("010-1234-5678")
                        .build();

        when(userUpdateService.update(anyLong(), any(UserUpdateRequest.class)))
                .thenReturn(userResponse);

        // when & then
        mockMvc.perform(
                        put("/users/{id}", userId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.name").value("수정된 이름"))
                .andExpect(jsonPath("$.data.phone").value("010-1234-5678"));
    }

    @Test
    void delete_shouldReturnOk() throws Exception {
        // given
        Long userId = 1L;

        // when & then
        mockMvc.perform(delete("/users/{id}", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").value("성공적으로 삭제되었습니다."));
    }
}
