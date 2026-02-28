package com.paystream.apigateway.config;

import com.paystream.apigateway.filter.JwtAuthenticationManager;
import com.paystream.apigateway.util.AuthenticationConverter;
import java.util.Arrays;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.SecurityWebFiltersOrder;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.authentication.AuthenticationWebFilter;
import org.springframework.security.web.server.context.NoOpServerSecurityContextRepository;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsConfigurationSource;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;
import reactor.core.publisher.Mono;

@Slf4j
@Configuration
@EnableWebFluxSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationManager jwtAuthenticationManager;
    private final AuthenticationConverter authenticationConverter;

    @Bean
    public SecurityWebFilterChain securityFilterChain(ServerHttpSecurity http) throws Exception {
        log.info("[SecurityConfig] Configuring SecurityFilterChain with CORS");
        AuthenticationWebFilter jwtFilter = new AuthenticationWebFilter(jwtAuthenticationManager);
        jwtFilter.setServerAuthenticationConverter(authenticationConverter);

        // 인증 실패 시 발생한 예외(한글 메시지 등)를 그대로 전파
        jwtFilter.setAuthenticationFailureHandler((exchange, e) -> Mono.error(e));

        http.csrf(ServerHttpSecurity.CsrfSpec::disable)
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .formLogin(ServerHttpSecurity.FormLoginSpec::disable)
                .httpBasic(ServerHttpSecurity.HttpBasicSpec::disable)
                .securityContextRepository(
                        NoOpServerSecurityContextRepository.getInstance()) // stateless
                .authorizeExchange(
                        exchange -> {
                            exchange.pathMatchers(
                                            HttpMethod.GET,
                                            "/*/stores",
                                            "/*/stores/**") // RESTFUL 방식으로 GET과 POST의 endpoint가 같아
                                    // 발생하는 문제 해결을 위해 작성
                                    .permitAll();
                            exchange.pathMatchers(allowAuthorizations()).permitAll();
                            exchange.anyExchange().authenticated();
                        })
                .addFilterAt(jwtFilter, SecurityWebFiltersOrder.AUTHENTICATION);

        return http.build();
    }

    /** 허용 URL 설정 (인증 로직을 거치지 않는 Endpoint) */
    @Bean
    public String[] allowAuthorizations() {
        return new String[] {
            // 공통 및 직접 접속 허용
            "/swagger-ui.html",
            "/swagger-ui/**",
            "/v3/api-docs/**",

            // 각 서비스별 Swagger 경로 허용
            // /api/inventories/swagger-ui/index.html 같은 패턴 대응
            "/api/*/swagger-ui/**",
            "/api/*/v3/api-docs",
            "/api/*/v3/api-docs/**",
            "/api/*/swagger-resources/**",
            "/webjars/**",
            "/*/webjars/**",
            "/api/*/ping",
            "/*/users/signup",
            "/*/users/login",
            "/*/users/refresh",
            "/*/payments/webhook",
            "/*/webjars/**",
        };
    }

    /** CORS 설정 (WebFlux용) React 프론트엔드에서 API Gateway로의 요청을 허용합니다. */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        configuration.setAllowedOriginPatterns(
                Arrays.asList(
                        "http://localhost:*",
                        "https://localhost:*",
                        "http://127.0.0.1:*",
                        "https://127.0.0.1:*"));

        configuration.setAllowedMethods(
                Arrays.asList(
                        HttpMethod.GET.name(),
                        HttpMethod.POST.name(),
                        HttpMethod.PUT.name(),
                        HttpMethod.DELETE.name(),
                        HttpMethod.OPTIONS.name(),
                        HttpMethod.PATCH.name(),
                        HttpMethod.HEAD.name()));

        configuration.setAllowedHeaders(
                Arrays.asList(
                        "Authorization",
                        "Content-Type",
                        "X-Auth-User-Id",
                        "X-Requested-With",
                        "Accept",
                        "Origin",
                        "Access-Control-Request-Method",
                        "Access-Control-Request-Headers"));

        configuration.setAllowCredentials(true);
        configuration.setExposedHeaders(Arrays.asList("*"));
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);

        return source;
    }
}
