package com.myproject.keycloak_service.it;

import com.myproject.keycloak_service.config.KeycloakConfig;
import com.myproject.keycloak_service.config.KeycloakTestContainerConfig;
import com.myproject.keycloak_service.dto.AccessTokenDTO;
import com.myproject.keycloak_service.dto.LoginDTO;
import com.myproject.keycloak_service.dto.UserDTO;
import com.myproject.keycloak_service.dto.UserInfoDTO;
import com.myproject.keycloak_service.service.UserService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@AutoConfigureWebTestClient
@Import({KeycloakTestContainerConfig.class, KeycloakConfig.class})
@TestInstance(TestInstance.Lifecycle.PER_METHOD)
public class ItUserControllerV1Tests {

    @Autowired
    WebTestClient webTestClient;

    @Autowired
    UserService userService;

    @Test
    @DisplayName("Test successful user registration functionality")
    public void givenUserDTO_whenRegistration_thenAccessTokeDTOReturned() {
        //given

        UserDTO userDTO = UserDTO.builder()
                .email("successRegistration@test.com")
                .password("password")
                .confirmedPassword("password")
                .build();

        //when
        WebTestClient.ResponseSpec result = webTestClient.post()
                .uri("/v1/auth/registration")
                .contentType(MediaType.APPLICATION_JSON)
                .body(Mono.just(userDTO), UserDTO.class)
                .exchange();

        //then
        result.expectStatus().isOk()
                .expectBody(AccessTokenDTO.class)
                .consumeWith(System.out::println);
    }

    @Test
    @DisplayName("Test failed user registration functionality")
    public void givenIncorrectUserDTO_whenRegistration_thenExceptionReturned() {
        //given

        UserDTO userDTO = UserDTO.builder()
                .email("failedRegistration@test.com")
                .password("password")
                .confirmedPassword("passwordd")
                .build();


        //when
        WebTestClient.ResponseSpec result = webTestClient.post()
                .uri("/v1/auth/registration")
                .contentType(MediaType.APPLICATION_JSON)
                .body(Mono.just(userDTO), UserDTO.class)
                .exchange();

        //then
        result.expectStatus().isBadRequest()
                .expectBody()
                .consumeWith(System.out::println)
                .jsonPath("$.error_message").isEqualTo("Provided bad credentials");
    }

    @Test
    @DisplayName("Test user login functionality")
    public void givenLoginDTO_whenLogin_thenAccessTokenDTOReturned() {
        //given

        UserDTO userDTO = UserDTO.builder()
                .email("successLogins@test.com")
                .password("password")
                .confirmedPassword("password")
                .build();
        userService.registerUser(userDTO).map(AccessTokenDTO::accessToken).block();

        LoginDTO dto = LoginDTO.builder()
                .userName("successLogins@test.com")
                .password("password")
                .build();

        //when
        WebTestClient.ResponseSpec result = webTestClient.post()
                .uri("/v1/login")
                .contentType(MediaType.APPLICATION_JSON)
                .body(Mono.just(dto), UserDTO.class)
                .exchange();

        //then
        result.expectStatus().isOk()
                .expectBody(AccessTokenDTO.class)
                .consumeWith(System.out::println);
    }

    @Test
    @DisplayName("Test failed user login functionality")
    public void givenIncorrectLoginDTO_whenLogin_thenExceptionReturned() {
        //given
        UserDTO userDTO = UserDTO.builder()
                .email("failedLogins@test.com")
                .password("password")
                .confirmedPassword("password")
                .build();
        userService.registerUser(userDTO).map(AccessTokenDTO::accessToken).block();

        LoginDTO dto = LoginDTO.builder()
                .userName("failedLogins@test.com")
                .password("passwordd")
                .build();

        //when
        WebTestClient.ResponseSpec result = webTestClient.post()
                .uri("/v1/login")
                .contentType(MediaType.APPLICATION_JSON)
                .body(Mono.just(dto), UserDTO.class)
                .exchange();

        //then
        result.expectStatus().isBadRequest()
                .expectBody()
                .consumeWith(System.out::println)
                .jsonPath("$.error_message").isEqualTo("Failed to login " + dto.getUserName() + ", check your credentials");
    }

    @Test
    @DisplayName("Test get user details functionality")
    public void givenUserName_whenGetUserData_thenUserInfoDTOReturned() {
        //given

        UserDTO userDTO = UserDTO.builder()
                .email("successUserDetails@test.com")
                .password("password")
                .confirmedPassword("password")
                .build();
        String token = userService.registerUser(userDTO).map(AccessTokenDTO::accessToken).block();

        String userName = "successUserDetails@test.com";

        //when
        WebTestClient.ResponseSpec result = webTestClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/v1/username")
                        .queryParam("username", userName)
                        .build())
                .header("Authorization", "Bearer " + token)
                .exchange();

        //then
        result.expectStatus().isOk()
                .expectBody(UserInfoDTO.class)
                .consumeWith(System.out::println);
    }

    @Test
    @DisplayName("Test failed get user details functionality")
    public void givenIncorrectUserName_whenGetUserData_thenExceptionReturned() {
        //given

        UserDTO userDTO = UserDTO.builder()
                .email("failedUserDetails@test.com")
                .password("password")
                .confirmedPassword("password")
                .build();
        String token = userService.registerUser(userDTO).map(AccessTokenDTO::accessToken).block();

        String userName = "incorrectUserName";

        //when
        WebTestClient.ResponseSpec result = webTestClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/v1/username")
                        .queryParam("username", userName)
                        .build())
                .header("Authorization", "Bearer " + token)
                .exchange();

        //then
        result.expectStatus().isBadRequest()
                .expectBody()
                .consumeWith(System.out::println)
                .jsonPath("$.error_message").isEqualTo("User is not found");
    }


}
