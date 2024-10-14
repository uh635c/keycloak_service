package com.myproject.keycloak_service.config;

import org.keycloak.OAuth2Constants;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class KeycloakConfig {

    @Value("${keycloak.auth-server-url}")
    public String serverURL;
    @Value("${keycloak.realm}")
    public String realm;
    @Value("${keycloak.client-id}")
    public String clientID;
    @Value("${keycloak.client-secret}")
    public String clientSecret;
    @Value("${keycloak.username-admin-client}")
    public String username;
    @Value("${keycloak.password-admin-client}")
    public String password;


    @Bean
    public Keycloak keycloak(){
       return KeycloakBuilder.builder()
                    .serverUrl(serverURL)
                    .realm(realm)
                    .grantType(OAuth2Constants.CLIENT_CREDENTIALS)
                    .clientId(clientID)
                    .clientSecret(clientSecret)
                    .username(username)
                    .password(password)
                    .build();
    }

}
