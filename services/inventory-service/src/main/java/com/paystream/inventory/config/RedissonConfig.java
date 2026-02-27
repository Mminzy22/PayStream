package com.paystream.inventory.config;

import lombok.RequiredArgsConstructor;
import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class RedissonConfig {

    @Value("${spring.data.redis.host}")
    private String host;

    @Value("${spring.data.redis.port}")
    private int port;

    @Bean
    public RedissonClient redissonClient() {
        Config config = new Config();
        config.useSingleServer() // 싱글 Redis 서버 모드 사용
                .setAddress("redis://" + host + ":" + port) // Redis 서버 주소
                .setConnectionMinimumIdleSize(5) // 최소 유휴 연결 수
                .setConnectionPoolSize(10) // 최대 커넥션 풀 크기
                .setIdleConnectionTimeout(10000) // 유휴 연결 타임 아웃 (ms)
                .setConnectTimeout(10000) // 연결 타임 아웃 (ms)
                .setTimeout(3000) // 명령 실행 타임아웃 (ms)
                .setRetryAttempts(3) // 재시도 횟수
                .setRetryInterval(1500); // 재시도 간격 (ms)

        return Redisson.create(config); // RedissonClient 객체 생성 및 반환
    }
}
