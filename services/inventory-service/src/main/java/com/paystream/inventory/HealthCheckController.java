package com.paystream.inventory;

import java.time.LocalDateTime;
import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HealthCheckController {

    @GetMapping("/inventory/ping")
    public ResponseEntity<Map<String, Object>> ping() {
        return ResponseEntity.ok(
                Map.of(
                        "service", "inventory-service",
                        "status", "UP",
                        "timestamp", LocalDateTime.now()));
    }
}
