package com.paystream.notification.service.impl;

import com.paystream.core.exception.ExceptionEnum;
import com.paystream.core.exception.PayStreamException;
import com.paystream.notification.service.UserEmailProvider;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/** 기본 사용자 이메일 주소 조회 구현 - TODO: UserService와 통신하여 실제 이메일 주소 조회 */
@Slf4j
@Service
public class DefaultUserEmailProvider implements UserEmailProvider {

    /**
     * userId로 사용자 이메일 주소 조회
     *
     * @param userId 사용자 ID
     * @return 이메일 주소
     * @throws PayStreamException 사용자를 찾을 수 없거나 이메일을 가져올 수 없는 경우
     */
    @Override
    public String getEmailByUserId(Long userId) {
        // TODO: UserService와 통신하여 실제 이메일 주소 조회
        // 현재는 임시로 예외를 던짐 - 실제 구현 시 UserService의 Feign Client 또는 RestTemplate 사용
        log.warn("UserEmailProvider.getEmailByUserId() 미구현 - userId: {}", userId);
        throw new PayStreamException(ExceptionEnum.USER_NOT_FOUND);
    }
}
