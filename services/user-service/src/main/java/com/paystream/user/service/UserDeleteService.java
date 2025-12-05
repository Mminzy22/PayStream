package com.paystream.user.service;

import com.paystream.core.exception.ExceptionEnum;
import com.paystream.core.exception.PayStreamException;
import com.paystream.user.entity.User;
import com.paystream.user.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserDeleteService {

    private final UserRepository userRepository;

    @Transactional
    public void delete(Long id) {
        User user =
                userRepository
                        .findById(id)
                        .orElseThrow(() -> new PayStreamException(ExceptionEnum.USER_NOT_FOUND));

        userRepository.delete(user);
    }
}
