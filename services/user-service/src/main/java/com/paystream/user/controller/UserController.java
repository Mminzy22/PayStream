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

/** 사용자 관리 컨트롤러 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/users")
public class UserController {

    private final UserFindService userFindService;
    private final UserUpdateService userUpdateService;
    private final UserDeleteService userDeleteService;

    /** 사용자 조회 */
    @GetMapping("/{id}")
    public BaseResponse<UserResponse> findById(@PathVariable Long id) {
        UserResponse user = userFindService.findById(id);
        return BaseResponse.ok(user);
    }

    /** 사용자 정보 수정 */
    @PutMapping("/{id}")
    public BaseResponse<UserResponse> update(
            @PathVariable Long id, @Valid @RequestBody UserUpdateRequest request) {
        UserResponse response = userUpdateService.update(id, request);
        return BaseResponse.ok(response);
    }

    /** 사용자 삭제 */
    @DeleteMapping("/{id}")
    public BaseResponse<String> delete(@PathVariable Long id) {
        userDeleteService.delete(id);
        return BaseResponse.ok("성공적으로 삭제되었습니다.");
    }
}
