package com.paystream.notification.client;

import com.paystream.core.BaseResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/** UserService와 통신하기 위한 Feign Client */
@FeignClient(name = "user-service", path = "/users")
public interface UserServiceClient {

    /**
     * 사용자 ID로 사용자 정보 조회
     *
     * @param id 사용자 ID
     * @return 사용자 정보 응답
     */
    @GetMapping("/{id}")
    BaseResponse<UserInfoResponse> getUserById(@PathVariable Long id);

    /** 사용자 정보를 위한 내부 DTO */
    record UserInfoResponse(Long id, String email, String name, String phone) {}
}
