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

/** 인증 컨트롤러 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/users")
public class AuthController {

    private final AuthService authService;
    private final UserCreateService userCreateService;

    /** 회원가입 */
    @PostMapping("/signup")
    public BaseResponse<Long> signup(@Valid @RequestBody SignupRequest request) {
        Long id = userCreateService.create(request);
        return BaseResponse.created(id);
    }

    /** 로그인 */
    @PostMapping("/login")
    public BaseResponse<TokenResponse> login(@Valid @RequestBody LoginRequest request) {
        TokenResponse tokenResponse = authService.login(request);
        return BaseResponse.ok(tokenResponse);
    }

    /** 토큰 갱신 */
    @PostMapping("/refresh")
    public BaseResponse<TokenResponse> refreshToken(
            @Valid @RequestBody RefreshTokenRequest request) {
        TokenResponse tokenResponse = authService.refreshToken(request);
        return BaseResponse.ok(tokenResponse);
    }

    /** 로그아웃 */
    @PostMapping("/logout")
    public BaseResponse<String> logout(
            @RequestHeader(value = AUTHORIZATION, required = false) String authorization) {
        if (authorization == null || !authorization.startsWith("Bearer ")) {
            throw new IllegalArgumentException("Authorization 헤더에 Bearer 토큰이 필요합니다.");
        }

        String accessToken = authorization.substring(7);
        authService.logout(accessToken);
        return BaseResponse.ok("로그아웃되었습니다.");
    }
}
