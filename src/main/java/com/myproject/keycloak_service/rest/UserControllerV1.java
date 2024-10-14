package com.myproject.keycloak_service.rest;

import com.myproject.keycloak_service.dto.AccessTokenDTO;
import com.myproject.keycloak_service.dto.LoginDTO;
import com.myproject.keycloak_service.dto.UserDTO;
import com.myproject.keycloak_service.dto.UserInfoDTO;
import com.myproject.keycloak_service.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/v1")
@RequiredArgsConstructor
public class UserControllerV1 {

    private final UserService userService;

    @PostMapping("/auth/registration")
    public Mono<AccessTokenDTO> registration(@RequestBody UserDTO userDTO) {

        return userService.registerUser(userDTO);
    }

    @PostMapping("/login")
    public Mono<AccessTokenDTO> login(@RequestBody LoginDTO loginDTO) {

        return userService.login(loginDTO.userName(), loginDTO.password());
    }

    @GetMapping("/username")
    public Mono<UserInfoDTO> getUserData(@RequestParam("username") String username) {

        return userService.getUserInfo(username);
    }



}
