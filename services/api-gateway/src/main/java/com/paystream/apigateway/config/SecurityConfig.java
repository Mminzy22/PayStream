package com.paystream.apigateway.config;

import com.paystream.apigateway.properties.WhitelistProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.context.NoOpServerSecurityContextRepository;

@Slf4j
@Configuration
@EnableWebFluxSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    // 미인증 endpoint 목록
    private final WhitelistProperties whitelistProperties;

    @Bean
    public SecurityWebFilterChain securityFilterChain(ServerHttpSecurity http) throws Exception {
        log.info("[AuthenticationFilter] Configuring SecurityFilterChain");
        http.csrf(csrf -> csrf.disable())
                .formLogin(ServerHttpSecurity.FormLoginSpec::disable)
                .httpBasic(ServerHttpSecurity.HttpBasicSpec::disable)
                .securityContextRepository(
                        NoOpServerSecurityContextRepository.getInstance()) // stateless
                .authorizeExchange(
                        exchange -> {
                            exchange.anyExchange().permitAll();
                        });

        return http.build();
    }
}
