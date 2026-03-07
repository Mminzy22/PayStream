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

/** 인증 서비스 */
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final TokenBlacklistService tokenBlacklistService;

    @Transactional
    public Long signup(SignupRequest request) {
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

        if (user.getPassword() == null
                || !passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new PayStreamException(ExceptionEnum.INVALID_CREDENTIALS);
        }

        String accessToken = jwtUtil.generateAccessToken(user.getId(), user.getEmail());
        String refreshToken = jwtUtil.generateRefreshToken(user.getId(), user.getEmail());

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
        if (!jwtUtil.validateToken(request.getRefreshToken(), "refresh")) {
            throw new PayStreamException(ExceptionEnum.INVALID_REFRESH_TOKEN);
        }

        Long userId = jwtUtil.extractUserId(request.getRefreshToken());
        User user =
                userRepository
                        .findById(userId)
                        .orElseThrow(() -> new PayStreamException(ExceptionEnum.USER_NOT_FOUND));

        if (!user.getRefreshToken().equals(request.getRefreshToken())) {
            throw new PayStreamException(ExceptionEnum.INVALID_REFRESH_TOKEN);
        }

        if (user.getRefreshTokenExpiryDate() == null
                || user.getRefreshTokenExpiryDate().isBefore(LocalDateTime.now())) {
            throw new PayStreamException(ExceptionEnum.EXPIRED_REFRESH_TOKEN);
        }

        String accessToken = jwtUtil.generateAccessToken(user.getId(), user.getEmail());
        String refreshToken = jwtUtil.generateRefreshToken(user.getId(), user.getEmail());

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
        if (!jwtUtil.validateToken(accessToken, "access")) {
            throw new PayStreamException(ExceptionEnum.INVALID_CREDENTIALS);
        }

        Long userId = jwtUtil.extractUserId(accessToken);
        User user =
                userRepository
                        .findById(userId)
                        .orElseThrow(() -> new PayStreamException(ExceptionEnum.USER_NOT_FOUND));

        tokenBlacklistService.addToBlacklist(accessToken);
        user.clearRefreshToken();
    }
}
