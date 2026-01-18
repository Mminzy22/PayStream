package com.paystream.payment.client;

import com.paystream.core.BaseResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "user-service", path = "/users")
public interface UserServiceClient {

    @GetMapping("/{id}")
    BaseResponse<UserInfoResponse> getUserById(@PathVariable Long id);

    /** User 정보를 위한 내부 DTO */
    record UserInfoResponse(Long id, String email, String name, String phone) {}
}
