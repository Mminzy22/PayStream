package com.paystream.notification.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

/** 이메일 발송 서비스 - Spring Mail을 사용하여 이메일 발송 */
@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    /**
     * 이메일 발송
     *
     * @param to 수신자 이메일 주소
     * @param subject 이메일 제목
     * @param body 이메일 본문
     * @throws MessagingException 이메일 발송 실패 시
     */
    public void sendEmail(String to, String subject, String body) throws MessagingException {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(body, true); // HTML 형식 지원

            mailSender.send(message);
            log.info("이메일 발송 성공 - 수신자: {}, 제목: {}", to, subject);
        } catch (MessagingException e) {
            log.error("이메일 발송 실패 - 수신자: {}, 제목: {}, 오류: {}", to, subject, e.getMessage());
            throw e;
        }
    }
}
