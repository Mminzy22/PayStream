package com.paystream.user.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.paystream.user.dto.request.LoginRequest;
import com.paystream.user.dto.request.RefreshTokenRequest;
import com.paystream.user.dto.request.SignupRequest;
import com.paystream.user.dto.response.TokenResponse;
import com.paystream.user.service.AuthService;
import com.paystream.user.service.UserCreateService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(
        controllers = AuthController.class,
        excludeAutoConfiguration = {
            org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration.class
        })
@TestPropertySource(
        properties = {
            "eureka.client.enabled=false",
            "eureka.client.register-with-eureka=false",
            "eureka.client.fetch-registry=false"
        })
class AuthControllerTest {

    @Autowired private MockMvc mockMvc;

    @Autowired private ObjectMapper objectMapper;

    @MockBean private AuthService authService;

    @MockBean private UserCreateService userCreateService;

    @Test
    void signupShouldReturnCreated() throws Exception {
        // given
        SignupRequest request =
                SignupRequest.builder()
                        .email("test@example.com")
                        .name("테스트 사용자")
                        .password("password123")
                        .termsOfServiceAgreed(true)
                        .privacyPolicyAgreed(true)
                        .build();

        when(userCreateService.create(any(SignupRequest.class))).thenReturn(1L);

        // when & then
        mockMvc.perform(
                        post("/users/signup")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data").value(1L));
    }

    @Test
    void signupWithInvalidRequestShouldReturnBadRequest() throws Exception {
        // given
        SignupRequest request =
                SignupRequest.builder()
                        .email("invalid-email") // 잘못된 이메일 형식
                        .name("") // 빈 이름
                        .password("")
                        .build();

        // when & then
        mockMvc.perform(
                        post("/users/signup")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void loginShouldReturnOk() throws Exception {
        // given
        LoginRequest request =
                LoginRequest.builder().email("test@example.com").password("password123").build();

        TokenResponse tokenResponse =
                TokenResponse.builder()
                        .accessToken("access-token")
                        .refreshToken("refresh-token")
                        .expiresIn(3600L)
                        .build();

        when(authService.login(any(LoginRequest.class))).thenReturn(tokenResponse);

        // when & then
        mockMvc.perform(
                        post("/users/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.accessToken").value("access-token"))
                .andExpect(jsonPath("$.data.refreshToken").value("refresh-token"));
    }

    @Test
    void refreshTokenShouldReturnOk() throws Exception {
        // given
        RefreshTokenRequest request =
                RefreshTokenRequest.builder().refreshToken("refresh-token").build();

        TokenResponse tokenResponse =
                TokenResponse.builder()
                        .accessToken("new-access-token")
                        .refreshToken("new-refresh-token")
                        .expiresIn(3600L)
                        .build();

        when(authService.refreshToken(any(RefreshTokenRequest.class))).thenReturn(tokenResponse);

        // when & then
        mockMvc.perform(
                        post("/users/refresh")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.accessToken").value("new-access-token"));
    }

    @Test
    void logoutWithValidTokenShouldReturnOk() throws Exception {
        // given
        String accessToken = "Bearer valid-access-token";

        // when & then
        mockMvc.perform(
                        post("/users/logout")
                                .header("Authorization", accessToken)
                                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").value("로그아웃되었습니다."));
    }

    @Test
    void logoutWithoutTokenShouldReturnBadRequest() throws Exception {
        // when & then
        mockMvc.perform(post("/users/logout").contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }
}
