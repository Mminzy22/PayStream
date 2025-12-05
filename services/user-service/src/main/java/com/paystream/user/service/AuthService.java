package com.paystream.user.service;

import com.paystream.core.exception.ExceptionEnum;
import com.paystream.core.exception.PayStreamException;
import com.paystream.user.dto.request.LoginRequest;
import com.paystream.user.dto.request.RefreshTokenRequest;
import com.paystream.user.dto.request.SignupRequest;
import com.paystream.user.dto.response.TokenResponse;
import com.paystream.user.entity.User;
import com.paystream.user.repository.UserRepository;
import com.paystream.user.util.JwtUtil;
import jakarta.transaction.Transactional;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final TokenBlacklistService tokenBlacklistService;

    @Transactional
    public Long signup(SignupRequest request) {
        // 이메일 중복 확인
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new PayStreamException(ExceptionEnum.USER_ALREADY_EXISTS);
        }

        User user = request.toEntity(passwordEncoder);
        return userRepository.save(user).getId();
    }

    @Transactional
    public TokenResponse login(LoginRequest request) {
        User user =
                userRepository
                        .findByEmail(request.getEmail())
                        .orElseThrow(
                                () -> new PayStreamException(ExceptionEnum.INVALID_CREDENTIALS));

        // 비밀번호 확인
        if (user.getPassword() == null
                || !passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new PayStreamException(ExceptionEnum.INVALID_CREDENTIALS);
        }

        // 이메일 인증 확인 (나중에 이메일 인증 기능 추가 시 활성화)
        // if (!user.getEmailVerified()) {
        //     throw new PayStreamException(ExceptionEnum.EMAIL_NOT_VERIFIED);
        // }

        // JWT 토큰 생성
        String accessToken = jwtUtil.generateAccessToken(user.getId(), user.getEmail());
        String refreshToken = jwtUtil.generateRefreshToken(user.getId(), user.getEmail());

        // 리프레시 토큰 저장
        LocalDateTime refreshTokenExpiry =
                LocalDateTime.now().plusSeconds(jwtUtil.getRefreshTokenExpiration() / 1000);
        user.updateRefreshToken(refreshToken, refreshTokenExpiry);

        return TokenResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .expiresIn(jwtUtil.getAccessTokenExpiration() / 1000)
                .build();
    }

    @Transactional
    public TokenResponse refreshToken(RefreshTokenRequest request) {
        // 리프레시 토큰 검증
        if (!jwtUtil.validateToken(request.getRefreshToken(), "refresh")) {
            throw new PayStreamException(ExceptionEnum.INVALID_REFRESH_TOKEN);
        }

        Long userId = jwtUtil.extractUserId(request.getRefreshToken());
        User user =
                userRepository
                        .findById(userId)
                        .orElseThrow(() -> new PayStreamException(ExceptionEnum.USER_NOT_FOUND));

        // 저장된 리프레시 토큰과 일치하는지 확인
        if (!user.getRefreshToken().equals(request.getRefreshToken())) {
            throw new PayStreamException(ExceptionEnum.INVALID_REFRESH_TOKEN);
        }

        // 리프레시 토큰 만료 확인
        if (user.getRefreshTokenExpiryDate() == null
                || user.getRefreshTokenExpiryDate().isBefore(LocalDateTime.now())) {
            throw new PayStreamException(ExceptionEnum.EXPIRED_REFRESH_TOKEN);
        }

        // 새로운 토큰 생성
        String accessToken = jwtUtil.generateAccessToken(user.getId(), user.getEmail());
        String refreshToken = jwtUtil.generateRefreshToken(user.getId(), user.getEmail());

        // 리프레시 토큰 업데이트
        LocalDateTime refreshTokenExpiry =
                LocalDateTime.now().plusSeconds(jwtUtil.getRefreshTokenExpiration() / 1000);
        user.updateRefreshToken(refreshToken, refreshTokenExpiry);

        return TokenResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .expiresIn(jwtUtil.getAccessTokenExpiration() / 1000)
                .build();
    }

    @Transactional
    public void logout(String accessToken) {
        // 액세스 토큰 검증
        if (!jwtUtil.validateToken(accessToken, "access")) {
            throw new PayStreamException(ExceptionEnum.INVALID_CREDENTIALS);
        }

        // 액세스 토큰에서 사용자 ID 추출
        Long userId = jwtUtil.extractUserId(accessToken);
        User user =
                userRepository
                        .findById(userId)
                        .orElseThrow(() -> new PayStreamException(ExceptionEnum.USER_NOT_FOUND));

        // 액세스 토큰을 블랙리스트에 추가 (즉시 로그아웃)
        tokenBlacklistService.addToBlacklist(accessToken);

        // 리프레시 토큰 삭제 (새로운 토큰 발급 방지)
        user.clearRefreshToken();
    }
}
