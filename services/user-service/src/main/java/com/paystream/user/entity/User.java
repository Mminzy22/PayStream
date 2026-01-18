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

    @Column(nullable = true)
    private String password;

    private String phone;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private AuthProvider provider = AuthProvider.LOCAL;

    @Column(nullable = true)
    private String providerId;

    @Column(nullable = false)
    @Builder.Default
    private Boolean emailVerified = false;

    @Column(nullable = true)
    private String emailVerificationCode;

    @Column(nullable = true)
    private LocalDateTime emailVerificationCodeExpiry;

    @Column(nullable = true)
    private LocalDateTime emailVerifiedAt;

    @Column(nullable = false)
    @Builder.Default
    private Boolean termsOfServiceAgreed = false;

    @Column(nullable = false)
    @Builder.Default
    private Boolean privacyPolicyAgreed = false;

    @Column(nullable = false)
    @Builder.Default
    private Boolean marketingAgreed = false;

    @Column(nullable = true)
    private LocalDateTime termsAgreedAt;

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
