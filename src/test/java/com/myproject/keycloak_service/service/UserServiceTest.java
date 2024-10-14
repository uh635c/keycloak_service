package com.myproject.keycloak_service.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.myproject.keycloak_service.dto.AccessTokenDTO;
import com.myproject.keycloak_service.dto.UserDTO;
import com.myproject.keycloak_service.dto.UserInfoDTO;
import com.myproject.keycloak_service.exceptions.LoginFailedException;
import com.myproject.keycloak_service.exceptions.RegistrationFailedException;
import com.myproject.keycloak_service.exceptions.UserNotFoundException;
import com.myproject.keycloak_service.service.impl.UserServiceImpl;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.jboss.resteasy.core.ServerResponse;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.keycloak.representations.idm.UserRepresentation;
import org.mockito.BDDMockito;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.io.IOException;
import java.time.Instant;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {

    @Mock
    private KeycloakService keycloakService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    public static MockWebServer mockWebServer = new MockWebServer();
    String url = mockWebServer.url("/auth").toString();

    @Spy
    private WebClient webClient = WebClient.builder()
            .baseUrl(url)
            .build();

    @Spy
    @InjectMocks
    private UserServiceImpl userServiceTest;

    @BeforeAll
    public static void setUp() throws IOException {
        mockWebServer.start();
    }

    @AfterAll
    public static void tearDown() throws IOException {
        mockWebServer.shutdown();
    }

    @Test
    @DisplayName("Test get user information by username functionality")
    public void givenUsername_whenGetUserInfo_thenReturnUserInfo() {

        //given
        String username = "username";
        long dateInMillis = System.currentTimeMillis();

        UserRepresentation userRepresentation = new UserRepresentation();
        userRepresentation.setId("userId");
        userRepresentation.setEmail("username@email.com");
        userRepresentation.setCreatedTimestamp(dateInMillis);

        UserInfoDTO userInfo = UserInfoDTO.builder()
                .id("userId")
                .createdAt(Instant.ofEpochMilli(dateInMillis).atZone(ZoneId.systemDefault()).toLocalDateTime())
                .email("username@email.com")
                .build();

        BDDMockito.given(keycloakService.getUser(anyString())).willReturn(Mono.just(List.of(userRepresentation)));

        //when
        Mono<UserInfoDTO> obtainedUserRepresentation = userServiceTest.getUserInfo("username");

        //then
        StepVerifier.create(obtainedUserRepresentation)
                .assertNext(user -> {
                    verify(keycloakService, times(1)).getUser(anyString());
                    assertThat(user).isEqualTo(userInfo);
                })
                .verifyComplete();
    }

    @Test
    @DisplayName("Test get user by incorrect username functionality")
    public void givenIncorrectUsername_whenGetUserInfo_thenExceptionReturn() {

        //given
        BDDMockito.given(keycloakService.getUser(anyString())).willReturn(Mono.just(new ArrayList<UserRepresentation>()));

        //when
        Mono<UserInfoDTO> obtainedUserRepresentation = userServiceTest.getUserInfo("incorrect username");

        //then
        StepVerifier.create(obtainedUserRepresentation)
                .expectError(UserNotFoundException.class)
                .verify();
    }

    @Test
    @DisplayName("Test login user by username and password functionality")
    public void givenUsernameAndPassword_whenLoginUser_thenReturnAccessTokenDTO() throws JsonProcessingException {

        //given
        AccessTokenDTO accessTokenDTO = AccessTokenDTO.builder()
                .accessToken("accessToken")
                .expiresIn(System.currentTimeMillis())
                .refreshToken("refreshToken")
                .tokenType("Bearer")
                .build();

        mockWebServer.enqueue(new MockResponse().setResponseCode(HttpStatus.OK.value())
                .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                .setBody(objectMapper.writeValueAsString(accessTokenDTO)));


        //when
        Mono<AccessTokenDTO> obtainedToken = userServiceTest.login("username", "password");

        //then
        StepVerifier.create(obtainedToken)
                .expectNext(accessTokenDTO)
                .verifyComplete();
    }

    @Test
    @DisplayName("Test login user by incorrect username and password functionality")
    public void givenIncorrectUsernameAndPassword_whenLoginUser_thenReturnException() throws JsonProcessingException {

        //given
        mockWebServer.enqueue(new MockResponse().setResponseCode(HttpStatus.BAD_REQUEST.value()));

        //when
        Mono<AccessTokenDTO> obtainedToken = userServiceTest.login("username", "password");

        //then
        StepVerifier.create(obtainedToken)
                .expectError(LoginFailedException.class)
                .verify();
    }

    @Test
    @DisplayName("Test register user by email and password functionality")
    public void givenUsernameAndPassword_whenRegisterUser_thenReturnAccessTokenDTO() {

        //given
        UserDTO userDTO = UserDTO.builder()
                .email("username@email.com")
                .password("password")
                .confirmedPassword("password")
                .build();

        AccessTokenDTO accessTokenDTO = AccessTokenDTO.builder()
                .accessToken("accessToken")
                .expiresIn(System.currentTimeMillis())
                .refreshToken("refreshToken")
                .tokenType("Bearer")
                .build();

        ServerResponse response = new ServerResponse();
        response.setStatus(201);


        BDDMockito.given(keycloakService.addUser(any(UserDTO.class))).willReturn(Mono.just(response));
        BDDMockito.given(userServiceTest.login("username@email.com","password")).willReturn(Mono.just(accessTokenDTO));

        //when
        Mono<AccessTokenDTO> obtainedToken = userServiceTest.registerUser(userDTO);

        //then
        StepVerifier.create(obtainedToken)
                .expectNext(accessTokenDTO)
                .verifyComplete();
    }

    @Test
    @DisplayName("Test register user by email and incorrect confirmed password functionality")
    public void givenIncorrectConfirmedPassword_whenRegisterUser_thenReturnException() {

        //given
        UserDTO userDTO = UserDTO.builder()
                .email("username@email.com")
                .password("password")
                .confirmedPassword("passwordd")
                .build();

        //when
        Mono<AccessTokenDTO> obtainedToken = userServiceTest.registerUser(userDTO);

        //then
        StepVerifier.create(obtainedToken)
                .expectError(RegistrationFailedException.class)
                .verify();
    }

    @Test
    @DisplayName("Test user registration by email and password functionality")
    public void givenKeycloakResponseStatusNot201_whenRegisterUser_thenReturnException() {

        //given
        UserDTO userDTO = UserDTO.builder()
                .email("username@email.com")
                .password("password")
                .confirmedPassword("password")
                .build();

        ServerResponse response = new ServerResponse();
        response.setStatus(400);

        BDDMockito.given(keycloakService.addUser(any(UserDTO.class))).willReturn(Mono.just(response));

        //when
        Mono<AccessTokenDTO> obtainedToken = userServiceTest.registerUser(userDTO);

        //then
        StepVerifier.create(obtainedToken)
                .expectErrorMessage("User is not registered")
                .verify();
    }

}
