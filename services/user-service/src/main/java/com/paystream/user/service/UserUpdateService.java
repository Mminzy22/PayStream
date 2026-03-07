package com.paystream.user.service;

import com.paystream.core.exception.ExceptionEnum;
import com.paystream.core.exception.PayStreamException;
import com.paystream.user.dto.request.UserUpdateRequest;
import com.paystream.user.dto.response.UserResponse;
import com.paystream.user.entity.User;
import com.paystream.user.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/** 사용자 정보 수정 서비스 */
@Service
@RequiredArgsConstructor
public class UserUpdateService {

    private final UserRepository userRepository;

    @Transactional
    public UserResponse update(Long id, UserUpdateRequest request) {
        User user =
                userRepository
                        .findById(id)
                        .orElseThrow(() -> new PayStreamException(ExceptionEnum.USER_NOT_FOUND));

        user.update(request.getName(), request.getPhone());

        return UserResponse.of(user);
    }
}
