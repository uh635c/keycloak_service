package com.myproject.keycloak_service.config;

import dasniko.testcontainers.keycloak.KeycloakContainer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.DynamicPropertyRegistry;

@TestConfiguration(proxyBeanMethods = false)
public class KeycloakTestContainerConfig {

    @Value("${keycloak.realm}")
    private String realm;

    @Bean
    KeycloakContainer keycloakContainer(DynamicPropertyRegistry registry) {
        var keycloakContainer = new KeycloakContainer("quay.io/keycloak/keycloak:26.0.1").withRealmImportFile("realm-export.json");

        registry.add("spring.security.oauth2.resourceserver.jwt.issuer-uri",
                () -> keycloakContainer.getAuthServerUrl() + "/realms/" + realm);
        registry.add("keycloak.auth-server-url", keycloakContainer::getAuthServerUrl);
        registry.add("uri.token", () -> keycloakContainer.getAuthServerUrl() + "/realms/" + realm + "/protocol/openid-connect/token");

        return keycloakContainer;
    }

}
