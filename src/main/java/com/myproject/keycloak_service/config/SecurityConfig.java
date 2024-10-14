package com.myproject.keycloak_service.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class SecurityConfig {

    @Bean
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {
        return http
                .csrf((csrf) -> csrf.disable())
                .authorizeExchange(exchanges ->
                        exchanges
                                .pathMatchers("/v1/auth/registration", "/v1/login").permitAll()
                                .anyExchange().authenticated())
                .oauth2ResourceServer((oauth2) -> oauth2
                        .jwt(Customizer.withDefaults()))
                .build();

    }

    @Bean
    public WebClient webClient() {
        return WebClient.create();
    }
}
