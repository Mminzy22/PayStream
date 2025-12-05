package com.paystream.user.controller;

import static org.springframework.http.HttpHeaders.AUTHORIZATION;

import com.paystream.core.BaseResponse;
import com.paystream.user.dto.request.LoginRequest;
import com.paystream.user.dto.request.RefreshTokenRequest;
import com.paystream.user.dto.request.SignupRequest;
import com.paystream.user.dto.response.TokenResponse;
import com.paystream.user.service.AuthService;
import com.paystream.user.service.UserCreateService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/users")
public class AuthController {

    private final AuthService authService;
    private final UserCreateService userCreateService;

    @PostMapping("/signup")
    public BaseResponse<Long> signup(@Valid @RequestBody SignupRequest request) {
        Long id = userCreateService.create(request);
        return BaseResponse.created(id);
    }

    @PostMapping("/login")
    public BaseResponse<TokenResponse> login(@Valid @RequestBody LoginRequest request) {
        TokenResponse tokenResponse = authService.login(request);
        return BaseResponse.ok(tokenResponse);
    }

    @PostMapping("/refresh")
    public BaseResponse<TokenResponse> refreshToken(
            @Valid @RequestBody RefreshTokenRequest request) {
        TokenResponse tokenResponse = authService.refreshToken(request);
        return BaseResponse.ok(tokenResponse);
    }

    @PostMapping("/logout")
    public BaseResponse<String> logout(
            @RequestHeader(value = AUTHORIZATION, required = false) String authorization) {
        // Authorization 헤더에서 Bearer 토큰 추출
        if (authorization == null || !authorization.startsWith("Bearer ")) {
            throw new IllegalArgumentException("Authorization 헤더에 Bearer 토큰이 필요합니다.");
        }

        String accessToken = authorization.substring(7); // "Bearer " 제거
        authService.logout(accessToken);
        return BaseResponse.ok("로그아웃되었습니다.");
    }
}
