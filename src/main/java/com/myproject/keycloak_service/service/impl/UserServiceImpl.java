package com.myproject.keycloak_service.service.impl;

import com.myproject.keycloak_service.dto.AccessTokenDTO;
import com.myproject.keycloak_service.dto.UserDTO;
import com.myproject.keycloak_service.dto.UserInfoDTO;
import com.myproject.keycloak_service.exceptions.LoginFailedException;
import com.myproject.keycloak_service.exceptions.RegistrationFailedException;
import com.myproject.keycloak_service.exceptions.UserNotFoundException;
import com.myproject.keycloak_service.service.KeycloakService;
import com.myproject.keycloak_service.service.UserService;
import lombok.RequiredArgsConstructor;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.time.ZoneId;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final KeycloakService keycloakService;
    private final WebClient webClient;
    @Value("${uri.token}")
    private String tokenURI;
    @Value("${keycloak.client-id}")
    private String clientId;
    @Value("${keycloak.client-secret}")
    private String clientSecret;

    @Override
    public Mono<AccessTokenDTO> registerUser(UserDTO userDTO) {

        if (!userDTO.getPassword().equals(userDTO.getConfirmedPassword())) {
            return Mono.error(new RegistrationFailedException("Provided bad credentials", "REGISTRATION_FAILED"));
        }

        return keycloakService.addUser(userDTO)
                .flatMap(response -> {
                    if (response.getStatus() != 201) {
                        return Mono.error(new RegistrationFailedException("User is not registered", "REGISTRATION_FAILED"));
                    }

                    return login(userDTO.getEmail(), userDTO.getPassword());
                });
    }

    @Override
    public Mono<AccessTokenDTO> login(String username, String password) {
        MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
        formData.add("username", username);
        formData.add("password", password);
        formData.add("grant_type", "password");
        formData.add("client_id", clientId);
        formData.add("client_secret", clientSecret);

        return webClient.post()
                .uri(tokenURI)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(BodyInserters.fromFormData(formData))
                .retrieve()
                .bodyToMono(AccessTokenDTO.class)
                .onErrorResume(e ->Mono.error(new LoginFailedException("Failed to login " + username + ", check your credentials", "LOGIN_FAILED")));
    }

    @Override
    public Mono<UserInfoDTO> getUserInfo(String username) {

        return keycloakService.getUser(username)
                .flatMap(list -> {
                    if(list.isEmpty()){
                        return Mono.error(new UserNotFoundException("User is not found", "USER_NOT_FOUND"));
                    }
                    UserRepresentation user = list.getFirst();
                    return Mono.just(UserInfoDTO.builder()
                            .id(user.getId())
                            .email(user.getEmail())
                            .createdAt(Instant.ofEpochMilli(user.getCreatedTimestamp()).atZone(ZoneId.systemDefault()).toLocalDateTime())
                            .build());
                });
    }
}
