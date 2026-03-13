package com.paystream.order.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import java.util.List;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .components(components())
                .info(apiInfo())
                .servers(
                        List.of(
                                new Server()
                                        .url("http://localhost:8000/api")
                                        .description("API Gateway"),
                                new Server()
                                        .url("http://localhost:8083")
                                        .description("Order Service")));
    }

    private Info apiInfo() {
        return new Info().title("Order API").description("Order API").version("0.0.1-SNAPSHOT");
    }

    private Components components() {
        return new Components()
                .addSecuritySchemes(
                        "accessToken",
                        new SecurityScheme()
                                .name("accessToken")
                                .type(SecurityScheme.Type.APIKEY)
                                .in(SecurityScheme.In.HEADER)
                                .bearerFormat("JWT"))
                .addSecuritySchemes(
                        "refreshToken",
                        new SecurityScheme()
                                .name("refreshToken")
                                .type(SecurityScheme.Type.APIKEY)
                                .in(SecurityScheme.In.HEADER)
                                .bearerFormat("JWT"))
                .addSecuritySchemes(
                        "X-Auth-User-Id",
                        new SecurityScheme()
                                .name("X-Auth-User-Id")
                                .type(SecurityScheme.Type.APIKEY)
                                .in(SecurityScheme.In.HEADER));
    }
}
