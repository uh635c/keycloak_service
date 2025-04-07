package com.myproject.keycloak_service.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.myproject.keycloak_service.client.KeycloakClient;
import com.myproject.keycloak_service.client.PersonServiceClient;
import com.myproject.keycloak_service.dto.AccessTokenDTO;
import com.myproject.keycloak_service.dto.KeycloakUserDTO;
import com.myproject.keycloak_service.exceptions.RegistrationFailedException;
import com.myproject.keycloak_service.exceptions.UserNotFoundException;
import com.myproject.keycloak_service.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import ru.uh635c.dto.IndividualResponseDTO;
import ru.uh635c.dto.UserRegistrationDTO;

import java.util.Base64;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final PersonServiceClient personServiceClient;
    private final KeycloakClient keycloakClient;

    @Override
    public Mono<AccessTokenDTO> registerUser(UserRegistrationDTO userDTO) {

        if (!userDTO.getPassword().equals(userDTO.getConfirmedPassword())) {
            return Mono.error(new RegistrationFailedException("Provided bad credentials", "REGISTRATION_FAILED"));
        }

        return personServiceClient.registerUser(userDTO)
                .flatMap(savedUser -> keycloakClient.addUser(KeycloakUserDTO.builder()
                                .guid(savedUser.getId())
                                .email(savedUser.getEmail())
                                .firstName(savedUser.getFirstName())
                                .lastName(savedUser.getLastName())
                                .password(userDTO.getPassword())
                                .build())
                        .flatMap(response -> {
                            if (response.getStatus() != 201) {
                                return personServiceClient.deleteUser(savedUser.getId())
                                        .then(Mono.error(new RegistrationFailedException("User is not registered, try later", "REGISTRATION_FAILED")));
                            }

                            return keycloakClient.login(userDTO.getEmail(), userDTO.getPassword());
                        }));


    }

    @Override
    public Mono<AccessTokenDTO> login(String username, String password) {
        return keycloakClient.login(username, password);
    }

    @Override
    public Mono<IndividualResponseDTO> getUserInfo(ServerWebExchange exchange) {

        String token = exchange.getRequest().getHeaders().getFirst("Authorization").substring(8);
        String[] chunks = token.split("\\.");
        Base64.Decoder decoder = Base64.getDecoder();
        String payload = new String(decoder.decode(chunks[1]));
        ObjectMapper objectMapper = new ObjectMapper();
        String guid;
        try {
            guid = (String) objectMapper.readValue(payload, Map.class).get("Guid");
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

        if (!StringUtils.hasText(guid)) {
            return Mono.error(new UserNotFoundException("Something went wrong, guid not found", "USER_NOT_FOUND"));
        }
        return personServiceClient.getUser(guid);
    }
}
