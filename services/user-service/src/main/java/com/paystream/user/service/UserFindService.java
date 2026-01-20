package com.paystream.user.service;

import com.paystream.core.exception.ExceptionEnum;
import com.paystream.core.exception.PayStreamException;
import com.paystream.user.dto.response.UserResponse;
import com.paystream.user.entity.User;
import com.paystream.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** 사용자 조회 서비스 */
@Transactional(readOnly = true)
@Service
@RequiredArgsConstructor
public class UserFindService {

    private final UserRepository userRepository;

    public UserResponse findById(Long id) {
        User user =
                userRepository
                        .findById(id)
                        .orElseThrow(() -> new PayStreamException(ExceptionEnum.USER_NOT_FOUND));

        return UserResponse.of(user);
    }

    public UserResponse findByEmail(String email) {
        User user =
                userRepository
                        .findByEmail(email)
                        .orElseThrow(() -> new PayStreamException(ExceptionEnum.USER_NOT_FOUND));

        return UserResponse.of(user);
    }
}
