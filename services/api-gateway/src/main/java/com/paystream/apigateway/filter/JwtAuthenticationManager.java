package com.paystream.apigateway.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.paystream.apigateway.util.JwtUtil;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import java.util.Collections;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationManager implements ReactiveAuthenticationManager {

    private final JwtUtil jwtUtil;
    private final ObjectMapper objectMapper;

    @Override
    public Mono<Authentication> authenticate(Authentication authentication) {
        String token = authentication.getCredentials().toString();

        try {
            if (jwtUtil.validateToken(token, "access")) {
                Claims claims = jwtUtil.extractClaims(token);

                // 인증 완료된 UsernamePasswordAuthenticationToken 반환
                return Mono.just(
                        new UsernamePasswordAuthenticationToken(
                                claims, null, Collections.emptyList()));
            } else {
                return Mono.error(new BadCredentialsException("유효하지 않은 토큰입니다. 다시 로그인해주세요."));
            }
        } catch (ExpiredJwtException e) {
            return Mono.error(new BadCredentialsException("토큰 유효 기간이 만료되었습니다."));
        } catch (MalformedJwtException e) {
            return Mono.error(new BadCredentialsException("변조되었거나 형식이 잘못된 토큰입니다."));
        } catch (Exception e) {
            // 그 외 예상치 못한 에러
            return Mono.error(new BadCredentialsException("인증 처리 중 오류가 발생했습니다: " + e.getMessage()));
        }
    }
}
