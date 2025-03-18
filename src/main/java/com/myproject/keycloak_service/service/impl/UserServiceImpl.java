package com.myproject.keycloak_service.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.myproject.keycloak_service.dto.AccessTokenDTO;
import com.myproject.keycloak_service.dto.KeycloakUserDTO;
import com.myproject.keycloak_service.exceptions.LoginFailedException;
import com.myproject.keycloak_service.exceptions.RegistrationFailedException;
import com.myproject.keycloak_service.exceptions.UserNotFoundException;
import com.myproject.keycloak_service.mappers.UserMapper;
import com.myproject.keycloak_service.service.KeycloakService;
import com.myproject.keycloak_service.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import ru.uh635c.dto.IndividualResponseDTO;
import ru.uh635c.dto.UserRegistrationDTO;

import java.util.Base64;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final KeycloakService keycloakService;
    private final WebClient webClient;
    private final UserMapper userMapper;
    @Value("${uri.token}")
    private String tokenURI;
    @Value("${uri.person-service}")
    private String personServiceURI;
    @Value("${keycloak.client-id}")
    private String clientId;
    @Value("${keycloak.client-secret}")
    private String clientSecret;
    @Value("${jwt.header}")
    private String authorizationHeader;

    @Override
    public Mono<AccessTokenDTO> registerUser(UserRegistrationDTO userDTO) {

        if (!userDTO.getPassword().equals(userDTO.getConfirmedPassword())) {
            return Mono.error(new RegistrationFailedException("Provided bad credentials", "REGISTRATION_FAILED"));
        }

        return webClient.post()
                .uri(personServiceURI)
                .contentType(MediaType.APPLICATION_JSON)
                .body(Mono.just(userMapper.map(userDTO)), IndividualResponseDTO.class)
                .exchangeToMono(response -> {
                    if (response.statusCode().is2xxSuccessful()) {
                        return response.bodyToMono(IndividualResponseDTO.class);
                    }
                    return Mono.error(new UserNotFoundException("Something went wrong", "USER_NOT_FOUND"));
                })
                .flatMap(savedUser -> keycloakService.addUser(KeycloakUserDTO.builder()
                                .guid(savedUser.getId())
                                .email(savedUser.getEmail())
                                .firstName(savedUser.getFirstName())
                                .lastName(savedUser.getLastName())
                                .build())
                        .flatMap(response -> {
                            if (response.getStatus() != 201) {
                                return Mono.error(new RegistrationFailedException("User is not registered", "REGISTRATION_FAILED"));
                            }

                            return login(userDTO.getEmail(), userDTO.getPassword());
                        }));


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
                .onErrorResume(e -> Mono.error(new LoginFailedException("Failed to login " + username + ", check your credentials", "LOGIN_FAILED")));
    }

    @Override
    public Mono<IndividualResponseDTO> getUserInfo(ServerWebExchange exchange) {

        String token = exchange.getRequest().getHeaders().getFirst(authorizationHeader).substring(8);
        String[] chunks = token.split("\\.");
        Base64.Decoder decoder = Base64.getDecoder();
        String payload = new String(decoder.decode(chunks[1]));
        ObjectMapper objectMapper = new ObjectMapper();
        String gid;
        try {
            gid = (String) objectMapper.readValue(payload, Map.class).get("Guid");
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

        if (!StringUtils.hasText(gid)) {

            return Mono.error(new UserNotFoundException("There is no id provided", "USER_NOT_FOUND"));
        }
        return webClient.get()
                .uri(String.join("/", personServiceURI, gid))
                .exchangeToMono(response -> {
                    if (response.statusCode().is2xxSuccessful()) {
                        return response.bodyToMono(IndividualResponseDTO.class);
                    }
                    return Mono.error(new UserNotFoundException("Something went wrong", "USER_NOT_FOUND"));
                });


//        return keycloakService.getUser(id)
//                .flatMap(list -> {
//                    if(list.isEmpty()){
//                        return Mono.error(new UserNotFoundException("User is not found", "USER_NOT_FOUND"));
//                    }
//                    UserRepresentation user = list.getFirst();
//                    return Mono.just(UserInfoDTO.builder()
//                            .id(user.getId())
//                            .email(user.getEmail())
//                            .createdAt(Instant.ofEpochMilli(user.getCreatedTimestamp()).atZone(ZoneId.systemDefault()).toLocalDateTime())
//                            .build());
//                });
    }
}
