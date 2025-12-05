package com.paystream.user.entity;

import com.paystream.core.BaseEntity;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.*;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

@Getter
@Entity
@Table(name = "users")
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@DynamicUpdate
@DynamicInsert
@SQLRestriction("deleted = false")
@SQLDelete(sql = "update users set deleted = true, updated_at = CURRENT_TIMESTAMP where id = ?")
public class User extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String name;

    @Column(nullable = true) // 소셜 로그인 사용자는 비밀번호 없음
    private String password;

    private String phone;

    // 소셜 로그인 관련 (나중에 사용)
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private AuthProvider provider = AuthProvider.LOCAL; // 기본값: 일반 회원가입

    @Column(nullable = true) // 일반 회원가입 시 null
    private String providerId;

    // 이메일 인증 관련 (나중에 사용)
    @Column(nullable = false)
    @Builder.Default
    private Boolean emailVerified = false; // 기본값: 미인증

    @Column(nullable = true) // 이메일 인증 기능 추가 전까지 null
    private String emailVerificationCode;

    @Column(nullable = true)
    private LocalDateTime emailVerificationCodeExpiry;

    @Column(nullable = true)
    private LocalDateTime emailVerifiedAt;

    // 약관 동의 관련
    @Column(nullable = false)
    @Builder.Default
    private Boolean termsOfServiceAgreed = false; // 회원가입 시 true로 설정

    @Column(nullable = false)
    @Builder.Default
    private Boolean privacyPolicyAgreed = false; // 회원가입 시 true로 설정

    @Column(nullable = false)
    @Builder.Default
    private Boolean marketingAgreed = false; // 선택 약관, 기본값 false

    @Column(nullable = true)
    private LocalDateTime termsAgreedAt;

    // JWT 관련
    @Column(nullable = true)
    private String refreshToken;

    @Column(nullable = true)
    private LocalDateTime refreshTokenExpiryDate;

    public void updateRefreshToken(String refreshToken, LocalDateTime expiryDate) {
        this.refreshToken = refreshToken;
        this.refreshTokenExpiryDate = expiryDate;
    }

    public void clearRefreshToken() {
        this.refreshToken = null;
        this.refreshTokenExpiryDate = null;
    }

    public void update(String name, String phone) {
        this.name = name;
        this.phone = phone;
    }
}
