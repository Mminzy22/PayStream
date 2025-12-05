package com.paystream.user.dto.request;

import com.paystream.user.entity.User;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;

@Builder
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class SignupRequest {

    @NotBlank(message = "이메일은 필수 입력 항목입니다.")
    @Email(message = "올바른 이메일 형식이 아닙니다.")
    private String email;

    @NotBlank(message = "이름은 필수 입력 항목입니다.")
    private String name;

    @NotBlank(message = "비밀번호는 필수 입력 항목입니다.")
    private String password;

    private String phone;

    @jakarta.validation.constraints.NotNull(message = "필수 약관에 동의해야 합니다.")
    private Boolean termsOfServiceAgreed;

    @jakarta.validation.constraints.NotNull(message = "필수 약관에 동의해야 합니다.")
    private Boolean privacyPolicyAgreed;

    private Boolean marketingAgreed;

    public User toEntity(PasswordEncoder passwordEncoder) {
        return User.builder()
                .email(email)
                .name(name)
                .password(passwordEncoder.encode(password))
                .phone(phone)
                .termsOfServiceAgreed(termsOfServiceAgreed)
                .privacyPolicyAgreed(privacyPolicyAgreed)
                .marketingAgreed(marketingAgreed != null ? marketingAgreed : false)
                .termsAgreedAt(LocalDateTime.now())
                .build();
    }
}
