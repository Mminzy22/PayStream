package com.paystream.user.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Builder
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class TokenResponse {

    private String accessToken;
    private String refreshToken;

    @Builder.Default private String tokenType = "Bearer";

    private Long expiresIn; // 액세스 토큰 만료 시간 (초)
}
