package com.myproject.keycloak_service.utils;

import com.myproject.keycloak_service.dto.AccessTokenDTO;
import com.myproject.keycloak_service.dto.KeycloakUserDTO;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseCookie;
import org.springframework.util.MultiValueMap;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import ru.uh635c.dto.IndividualResponseDTO;
import ru.uh635c.dto.UserRegistrationDTO;
import org.jboss.resteasy.core.ServerResponse;

import java.time.LocalDateTime;

public class DataUtils {

    public static IndividualResponseDTO getIndividualResponseDto(){
        return IndividualResponseDTO.builder()
                .id("da431b33")
                .firstName("firstName")
                .lastName("lastName")
                .address("address")
                .zipCode("zipCode")
                .city("city")
                .state("state")
                .country("country")
                .phoneNumber("phone_number")
                .passportNumber("passport_number")
                .email("email@mail.com")
                .createdAt(LocalDateTime.of(2025,3,1,0,0))
                .updatedAt(LocalDateTime.of(2025,3,1,0,0))
                .build();
    }

    public static UserRegistrationDTO getUserRegistrationDTO(){
        return UserRegistrationDTO.builder()
                .firstName("firstName")
                .lastName("lastName")
                .password("password")
                .confirmedPassword("password")
                .address("address")
                .zipCode("zipCode")
                .city("city")
                .state("state")
                .country("country")
                .phoneNumber("phone_number")
                .passportNumber("passport_number")
                .email("email@mail.com")
                .build();
    }

    public static AccessTokenDTO getAccessTokenDTO(){
        return AccessTokenDTO.builder()
                .accessToken("accessToken")
                .expiresIn(System.currentTimeMillis())
                .refreshToken("refreshToken")
                .tokenType("Bearer")
                .build();
    }

    public static ServerResponse getServerResponse201(){
        ServerResponse response = new ServerResponse();
        response.setStatus(201);
        return response;
    }

    public static ServerResponse getServerResponse404(){
        ServerResponse response = new ServerResponse();
        response.setStatus(404);
        return response;
    }

    public static KeycloakUserDTO getKeycloakUserDTO(){
        return KeycloakUserDTO.builder()
                .guid("guid")
                .firstName("firstName")
                .lastName("lastName")
                .password("password")
                .email("email@mail.com")
                .build();
    }
}


