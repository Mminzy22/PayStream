package com.paystream.user.filter;

import com.paystream.user.service.TokenBlacklistService;
import com.paystream.user.util.JwtUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final TokenBlacklistService tokenBlacklistService;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String authHeader = request.getHeader("Authorization");

        // Authorization 헤더가 없거나 Bearer로 시작하지 않으면 다음 필터로
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            // Bearer 제거
            String token = authHeader.substring(7);

            // 블랙리스트 확인
            if (tokenBlacklistService.isBlacklisted(token)) {
                log.warn("블랙리스트에 등록된 토큰입니다: {}", token);
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.getWriter().write("{\"error\":\"토큰이 로그아웃되었습니다.\"}");
                return;
            }

            // 토큰 검증
            if (jwtUtil.validateToken(token, "access")) {
                // 사용자 ID 추출
                Long userId = jwtUtil.extractUserId(token);
                String email = jwtUtil.extractEmail(token);

                // SecurityContext에 인증 정보 설정
                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(
                                userId,
                                null,
                                java.util.Collections.singletonList(
                                        new SimpleGrantedAuthority("ROLE_USER")));
                authentication.setDetails(
                        new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authentication);

                log.debug("JWT 인증 성공: userId={}, email={}", userId, email);
            }
        } catch (Exception e) {
            log.error("JWT 인증 중 오류 발생", e);
            // 인증 실패 시 SecurityContext를 비우지 않고 다음 필터로 진행
            // (다른 필터나 SecurityConfig에서 처리)
        }

        filterChain.doFilter(request, response);
    }
}
