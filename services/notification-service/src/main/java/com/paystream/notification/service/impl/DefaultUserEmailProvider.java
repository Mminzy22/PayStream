package com.paystream.notification.service.impl;

import com.paystream.core.exception.ExceptionEnum;
import com.paystream.core.exception.PayStreamException;
import com.paystream.notification.client.UserServiceClient;
import com.paystream.notification.service.UserEmailProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/** 사용자 이메일 주소 조회 구현 - UserService와 통신하여 실제 이메일 주소 조회 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DefaultUserEmailProvider implements UserEmailProvider {

    private final UserServiceClient userServiceClient;

    /**
     * userId로 사용자 이메일 주소 조회
     *
     * @param userId 사용자 ID
     * @return 이메일 주소
     * @throws PayStreamException 사용자를 찾을 수 없거나 이메일을 가져올 수 없는 경우
     */
    @Override
    public String getEmailByUserId(Long userId) {
        try {
            UserServiceClient.UserInfoResponse userInfo =
                    userServiceClient.getUserById(userId).getData();

            if (userInfo == null || userInfo.email() == null || userInfo.email().isEmpty()) {
                log.warn("사용자 이메일을 찾을 수 없음 - userId: {}", userId);
                throw new PayStreamException(ExceptionEnum.USER_NOT_FOUND);
            }

            return userInfo.email();
        } catch (PayStreamException e) {
            throw e;
        } catch (Exception e) {
            log.error("UserService 통신 실패 - userId: {}, 오류: {}", userId, e.getMessage(), e);
            throw new PayStreamException(ExceptionEnum.USER_NOT_FOUND);
        }
    }
}
