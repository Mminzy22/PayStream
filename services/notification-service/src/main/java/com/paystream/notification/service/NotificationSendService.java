package com.paystream.notification.service;

import com.paystream.notification.domain.DeliveryLog;
import com.paystream.notification.domain.Notification;
import com.paystream.notification.domain.NotificationChannel;
import com.paystream.notification.domain.NotificationStatus;
import com.paystream.notification.repository.DeliveryLogRepository;
import com.paystream.notification.repository.NotificationRepository;
import jakarta.mail.MessagingException;
import jakarta.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

/** 알림 전송 서비스 - 스케줄러를 통해 대기 중인 알림을 전송 */
@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationSendService {

    private static final int MAX_RETRY_COUNT = 3; // 최대 재시도 횟수

    private final NotificationRepository notificationRepository;
    private final DeliveryLogRepository deliveryLogRepository;
    private final EmailService emailService;
    private final UserEmailProvider userEmailProvider;

    /** 전송 대기 중인 알림을 주기적으로 조회하여 전송 (30초마다 실행) */
    @Scheduled(fixedDelay = 30000) // 30초마다 실행
    public void sendPendingNotifications() {
        try {
            LocalDateTime now = LocalDateTime.now();
            // PENDING 상태이고 예약 시간이 지난 알림 조회
            List<Notification> pendingNotifications =
                    notificationRepository.findByStatusAndScheduledAtBefore(
                            NotificationStatus.PENDING, now);

            if (pendingNotifications.isEmpty()) {
                return;
            }

            log.info("전송 대기 중인 알림 {}개 발견", pendingNotifications.size());

            for (Notification notification : pendingNotifications) {
                try {
                    sendNotification(notification);
                } catch (Exception e) {
                    log.error(
                            "알림 전송 실패 - 알림 ID: {}, 오류: {}",
                            notification.getId(),
                            e.getMessage(),
                            e);
                    handleSendFailure(notification, e);
                }
            }
        } catch (Exception e) {
            log.error("알림 전송 처리 중 오류 발생: {}", e.getMessage(), e);
        }
    }

    /**
     * 알림 전송 처리
     *
     * @param notification 전송할 알림
     */
    @Transactional
    public void sendNotification(Notification notification) {
        // EMAIL 채널만 처리 (현재 이메일 알림만 지원)
        if (notification.getChannel() != NotificationChannel.EMAIL) {
            log.warn("지원하지 않는 알림 채널: {}", notification.getChannel());
            notification.setStatus(NotificationStatus.FAILED);
            notificationRepository.save(notification);
            return;
        }

        // DeliveryLog 생성 (전송 시도 기록)
        DeliveryLog deliveryLog = createDeliveryLog(notification, NotificationStatus.SENDING);

        try {
            // 사용자 이메일 주소 조회
            String recipientEmail = userEmailProvider.getEmailByUserId(notification.getUserId());

            // 이메일 전송
            notification.setStatus(NotificationStatus.SENDING);
            notificationRepository.save(notification);

            emailService.sendEmail(recipientEmail, notification.getTitle(), notification.getBody());

            // 전송 성공 처리
            notification.setStatus(NotificationStatus.SENT);
            notification.setSentAt(LocalDateTime.now());
            notificationRepository.save(notification);

            // DeliveryLog 업데이트 (성공)
            deliveryLog.setStatus(NotificationStatus.SENT);
            deliveryLog.setCompletedAt(LocalDateTime.now());
            deliveryLogRepository.save(deliveryLog);

            log.info("알림 전송 성공 - 알림 ID: {}, 수신자: {}", notification.getId(), recipientEmail);
        } catch (MessagingException e) {
            // 이메일 발송 실패 처리
            handleEmailSendFailure(notification, deliveryLog, e);
            throw new RuntimeException("이메일 발송 실패", e);
        } catch (Exception e) {
            // 기타 오류 처리
            handleEmailSendFailure(notification, deliveryLog, e);
            throw e;
        }
    }

    /**
     * 이메일 발송 실패 처리 (재시도 또는 실패 처리)
     *
     * @param notification 알림
     * @param deliveryLog 발송 이력
     * @param exception 발생한 예외
     */
    private void handleEmailSendFailure(
            Notification notification, DeliveryLog deliveryLog, Exception exception) {
        notification.incrementRetryCount();

        if (notification.getRetryCount() >= MAX_RETRY_COUNT) {
            // 최대 재시도 횟수 초과 - 실패 처리
            notification.setStatus(NotificationStatus.FAILED);
            log.warn(
                    "알림 전송 최종 실패 - 알림 ID: {}, 재시도 횟수: {}",
                    notification.getId(),
                    notification.getRetryCount());
        } else {
            // 재시도 가능 - PENDING 상태로 되돌림 (다음 스케줄러 실행 시 재시도)
            notification.setStatus(NotificationStatus.PENDING);
            // 재시도 간격을 위해 scheduledAt을 미래로 설정 (1분 후)
            notification.setScheduledAt(LocalDateTime.now().plusMinutes(1));
            log.info(
                    "알림 전송 실패 - 재시도 예약 - 알림 ID: {}, 재시도 횟수: {}",
                    notification.getId(),
                    notification.getRetryCount());
        }

        notificationRepository.save(notification);

        // DeliveryLog 업데이트 (실패)
        deliveryLog.setStatus(NotificationStatus.FAILED);
        deliveryLog.setErrorCode("EMAIL_SEND_FAILED");
        deliveryLog.setErrorMessage(
                exception.getMessage() != null
                        ? exception.getMessage()
                        : exception.getClass().getSimpleName());
        deliveryLog.setCompletedAt(LocalDateTime.now());
        deliveryLogRepository.save(deliveryLog);
    }

    /**
     * 전송 실패 처리 (일반 오류)
     *
     * @param notification 알림
     * @param exception 발생한 예외
     */
    private void handleSendFailure(Notification notification, Exception exception) {
        notification.incrementRetryCount();

        if (notification.getRetryCount() >= MAX_RETRY_COUNT) {
            notification.setStatus(NotificationStatus.FAILED);
        } else {
            notification.setStatus(NotificationStatus.PENDING);
            notification.setScheduledAt(LocalDateTime.now().plusMinutes(1));
        }

        notificationRepository.save(notification);
    }

    /**
     * DeliveryLog 생성
     *
     * @param notification 알림
     * @param status 상태
     * @return DeliveryLog
     */
    private DeliveryLog createDeliveryLog(Notification notification, NotificationStatus status) {
        DeliveryLog deliveryLog = new DeliveryLog();
        deliveryLog.setNotificationId(notification.getId());
        deliveryLog.setStatus(status);
        deliveryLog.setRequestedAt(LocalDateTime.now());
        return deliveryLogRepository.save(deliveryLog);
    }
}
