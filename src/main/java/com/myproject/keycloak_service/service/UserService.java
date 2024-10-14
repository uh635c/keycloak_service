package com.myproject.keycloak_service.service;

import com.myproject.keycloak_service.dto.AccessTokenDTO;
import com.myproject.keycloak_service.dto.UserDTO;
import com.myproject.keycloak_service.dto.UserInfoDTO;
import reactor.core.publisher.Mono;

public interface UserService {
    Mono<AccessTokenDTO> registerUser(UserDTO userDTO);
    Mono<AccessTokenDTO> login(String username, String password);
    Mono<UserInfoDTO> getUserInfo(String email);
}
