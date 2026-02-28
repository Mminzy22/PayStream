package com.paystream.notification.service;

/** 사용자 이메일 주소 조회 인터페이스 - UserService와 통신하여 사용자 이메일을 가져옴 */
public interface UserEmailProvider {

    /**
     * userId로 사용자 이메일 주소 조회
     *
     * @param userId 사용자 ID
     * @return 이메일 주소
     * @throws RuntimeException 사용자를 찾을 수 없거나 이메일을 가져올 수 없는 경우
     */
    String getEmailByUserId(Long userId);
}
