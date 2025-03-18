package com.myproject.keycloak_service.rest;

import com.myproject.keycloak_service.dto.AccessTokenDTO;
import com.myproject.keycloak_service.dto.LoginDTO;
import com.myproject.keycloak_service.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import ru.uh635c.dto.IndividualResponseDTO;
import ru.uh635c.dto.UserRegistrationDTO;

@RestController
@RequestMapping("/v1")
@RequiredArgsConstructor
public class UserControllerV1 {

    private final UserService userService;

    @PostMapping("/auth/registration")
    public Mono<AccessTokenDTO> registration(@RequestBody UserRegistrationDTO userDTO) {

        return userService.registerUser(userDTO);
    }

    @PostMapping("/login")
    public Mono<AccessTokenDTO> login(@RequestBody LoginDTO loginDTO) {

        return userService.login(loginDTO.userName(), loginDTO.password());
    }

    @GetMapping("/me")
    public Mono<IndividualResponseDTO> getUserData(ServerWebExchange exchange) {

        return userService.getUserInfo(exchange);
    }



}
