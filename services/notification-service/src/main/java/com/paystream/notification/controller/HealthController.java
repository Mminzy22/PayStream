package com.paystream.notification.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * 헬스 체크 컨트롤러
 * - 서비스 정상 동작 확인용 엔드포인트 제공
 */
@Slf4j
@RestController
@RequestMapping("/api/notifications")
public class HealthController {

    /**
     * 서비스 헬스 체크 엔드포인트
     * @return 서비스 상태 정보
     */
    @GetMapping("/ping")
    public ResponseEntity<Map<String, Object>> ping() {
        log.info("헬스 체크 요청 수신");
        
        Map<String, Object> response = new HashMap<>();
        response.put("service", "notification-service");
        response.put("status", "UP");
        response.put("timestamp", LocalDateTime.now());
        response.put("message", "알림 서비스가 정상 작동 중입니다.");
        
        return ResponseEntity.ok(response);
    }

    /**
     * 서비스 정보 조회 엔드포인트
     * @return 서비스 기본 정보
     */
    @GetMapping("/info")
    public ResponseEntity<Map<String, String>> info() {
        Map<String, String> info = new HashMap<>();
        info.put("name", "PayStream Notification Service");
        info.put("version", "0.0.1-SNAPSHOT");
        info.put("description", "이벤트 기반 알림 처리 서비스");
        
        return ResponseEntity.ok(info);
    }
}

