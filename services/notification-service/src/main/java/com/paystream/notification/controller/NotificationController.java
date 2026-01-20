package com.paystream.notification.controller;

import com.paystream.core.BaseResponse;
import com.paystream.notification.dto.NotificationRequest;
import com.paystream.notification.dto.NotificationResponse;
import com.paystream.notification.dto.NotificationUpdateRequest;
import com.paystream.notification.service.NotificationCreateService;
import com.paystream.notification.service.NotificationFindService;
import com.paystream.notification.service.NotificationUpdateService;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/** 알림 컨트롤러 - 알림 관련 REST API 엔드포인트 제공 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/notifications")
public class NotificationController {

    private final NotificationCreateService notificationCreateService;
    private final NotificationFindService notificationFindService;
    private final NotificationUpdateService notificationUpdateService;

    /**
     * 알림 생성
     *
     * @param request 알림 생성 요청
     * @return 생성된 알림 ID
     */
    @PostMapping
    @ResponseStatus(org.springframework.http.HttpStatus.CREATED)
    public BaseResponse<Long> create(@Valid @RequestBody NotificationRequest request) {
        Long id = notificationCreateService.create(request);
        return BaseResponse.created(id);
    }

    /**
     * 알림 조회 (ID로)
     *
     * @param id 알림 ID
     * @return 알림 정보
     */
    @GetMapping("/{id}")
    public BaseResponse<NotificationResponse> findById(@PathVariable Long id) {
        NotificationResponse response = notificationFindService.findById(id);
        return BaseResponse.ok(response);
    }

    /**
     * 사용자별 알림 목록 조회
     *
     * @param userId 사용자 ID
     * @return 알림 목록
     */
    @GetMapping("/users/{userId}")
    public BaseResponse<List<NotificationResponse>> findByUserId(@PathVariable Long userId) {
        List<NotificationResponse> responses = notificationFindService.findByUserId(userId);
        return BaseResponse.ok(responses);
    }

    /**
     * 알림 상태 업데이트 (읽음 처리 등)
     *
     * @param id 알림 ID
     * @param request 업데이트 요청
     * @return 업데이트된 알림 정보
     */
    @PutMapping("/{id}")
    public BaseResponse<NotificationResponse> update(
            @PathVariable Long id, @Valid @RequestBody NotificationUpdateRequest request) {
        NotificationResponse response = notificationUpdateService.update(id, request);
        return BaseResponse.ok(response);
    }
}
