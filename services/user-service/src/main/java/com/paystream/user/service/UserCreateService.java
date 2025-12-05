package com.paystream.user.service;

import com.paystream.core.exception.ExceptionEnum;
import com.paystream.core.exception.PayStreamException;
import com.paystream.user.dto.request.SignupRequest;
import com.paystream.user.entity.User;
import com.paystream.user.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserCreateService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public Long create(SignupRequest request) {
        // 이메일 중복 확인
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new PayStreamException(ExceptionEnum.USER_ALREADY_EXISTS);
        }

        User user = request.toEntity(passwordEncoder);
        return userRepository.save(user).getId();
    }
}
