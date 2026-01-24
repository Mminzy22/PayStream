package com.paystream.inventory.config;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import redis.embedded.RedisServer;

@SpringBootTest
@ActiveProfiles("test")
public abstract class IntegrationTestSupport {

    private static RedisServer redisServer;

    static {
        try {
            // 1. 프로세스 생성: 6379 포트를 사용하는 Redis 바이너리를 실행 준비
            redisServer = new RedisServer(6379);

            // 2. 서버 시작: 실제로 OS 레벨에서 포트를 열고 클라이언트를 받을 준비를 마침
            redisServer.start();

            System.out.println("✅ Embedded Redis started on port 6379");
        } catch (Exception e) {
            // 3. 예외 처리: 로컬에 이미 Redis(Docker 등)가 떠 있다면 포트 충돌이 발생함
            // 이 경우 이미 서버가 있는 것이므로 테스트를 계속 진행해도 무방함
            System.out.println("⚠️ Redis server is already running...: " + e.getMessage());
        }
    }
}
