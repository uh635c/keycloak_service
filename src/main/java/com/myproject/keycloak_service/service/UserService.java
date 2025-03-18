package com.myproject.keycloak_service.service;

import com.myproject.keycloak_service.dto.AccessTokenDTO;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import ru.uh635c.dto.IndividualResponseDTO;
import ru.uh635c.dto.UserRegistrationDTO;

public interface UserService {
    Mono<AccessTokenDTO> registerUser(UserRegistrationDTO userDTO);
    Mono<AccessTokenDTO> login(String username, String password);
    Mono<IndividualResponseDTO> getUserInfo(ServerWebExchange exchange);
}
