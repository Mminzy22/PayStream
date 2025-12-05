package com.paystream.user.controller;

import com.paystream.core.BaseResponse;
import com.paystream.user.dto.request.UserUpdateRequest;
import com.paystream.user.dto.response.UserResponse;
import com.paystream.user.service.UserDeleteService;
import com.paystream.user.service.UserFindService;
import com.paystream.user.service.UserUpdateService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/users")
public class UserController {

    private final UserFindService userFindService;
    private final UserUpdateService userUpdateService;
    private final UserDeleteService userDeleteService;

    @GetMapping("/{id}")
    public BaseResponse<UserResponse> findById(@PathVariable Long id) {
        UserResponse user = userFindService.findById(id);
        return BaseResponse.ok(user);
    }

    @PutMapping("/{id}")
    public BaseResponse<UserResponse> update(
            @PathVariable Long id, @Valid @RequestBody UserUpdateRequest request) {
        UserResponse response = userUpdateService.update(id, request);
        return BaseResponse.ok(response);
    }

    @DeleteMapping("/{id}")
    public BaseResponse<String> delete(@PathVariable Long id) {
        userDeleteService.delete(id);
        return BaseResponse.ok("성공적으로 삭제되었습니다.");
    }
}
