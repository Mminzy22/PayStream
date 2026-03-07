package com.paystream.user.service;

import com.paystream.user.util.JwtUtil;
import io.jsonwebtoken.Claims;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

/** 토큰 블랙리스트 관리 서비스 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TokenBlacklistService {

    private static final String BLACKLIST_PREFIX = "blacklist:token:";
    private final RedisTemplate<String, String> redisTemplate;
    private final JwtUtil jwtUtil;

    /**
     * 토큰을 블랙리스트에 추가
     *
     * @param token 블랙리스트에 추가할 토큰
     */
    public void addToBlacklist(String token) {
        try {
            Claims claims = jwtUtil.extractClaims(token);
            long expirationTime = claims.getExpiration().getTime();
            long currentTime = System.currentTimeMillis();
            long ttl = expirationTime - currentTime;

            if (ttl > 0) {
                String key = BLACKLIST_PREFIX + token;
                redisTemplate.opsForValue().set(key, "blacklisted", ttl, TimeUnit.MILLISECONDS);
                log.debug("토큰이 블랙리스트에 추가되었습니다. TTL: {}ms", ttl);
            }
        } catch (Exception e) {
            log.error("블랙리스트 추가 중 오류 발생", e);
            throw new RuntimeException("블랙리스트 추가 실패", e);
        }
    }

    /**
     * 토큰이 블랙리스트에 있는지 확인
     *
     * @param token 확인할 토큰
     * @return 블랙리스트에 있으면 true, 없으면 false
     */
    public boolean isBlacklisted(String token) {
        try {
            String key = BLACKLIST_PREFIX + token;
            Boolean exists = redisTemplate.hasKey(key);
            return Boolean.TRUE.equals(exists);
        } catch (Exception e) {
            log.error("블랙리스트 확인 중 오류 발생", e);
            return false;
        }
    }
}
