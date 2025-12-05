package com.paystream.user.dto.response;

import com.paystream.user.entity.AuthProvider;
import com.paystream.user.entity.User;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Builder
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class UserResponse {

    private Long id;
    private String email;
    private String name;
    private String phone;
    private AuthProvider provider;
    private Boolean emailVerified;
    private Boolean termsOfServiceAgreed;
    private Boolean privacyPolicyAgreed;
    private Boolean marketingAgreed;
    private LocalDateTime termsAgreedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static UserResponse of(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .name(user.getName())
                .phone(user.getPhone())
                .provider(user.getProvider())
                .emailVerified(user.getEmailVerified())
                .termsOfServiceAgreed(user.getTermsOfServiceAgreed())
                .privacyPolicyAgreed(user.getPrivacyPolicyAgreed())
                .marketingAgreed(user.getMarketingAgreed())
                .termsAgreedAt(user.getTermsAgreedAt())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
    }
}
