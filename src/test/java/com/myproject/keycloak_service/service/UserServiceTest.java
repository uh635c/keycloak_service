package com.myproject.keycloak_service.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.myproject.keycloak_service.client.KeycloakClient;
import com.myproject.keycloak_service.client.PersonServiceClient;
import com.myproject.keycloak_service.dto.AccessTokenDTO;
import com.myproject.keycloak_service.dto.KeycloakUserDTO;
import com.myproject.keycloak_service.exceptions.LoginFailedException;
import com.myproject.keycloak_service.exceptions.RegistrationFailedException;
import com.myproject.keycloak_service.exceptions.UserNotFoundException;
import com.myproject.keycloak_service.service.impl.UserServiceImpl;
import com.myproject.keycloak_service.utils.DataUtils;
import org.jboss.resteasy.core.ServerResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.BDDMockito;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import ru.uh635c.dto.IndividualResponseDTO;
import ru.uh635c.dto.UserRegistrationDTO;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {

    @Mock
    private KeycloakClient keycloakClient;
    @Mock
    private PersonServiceClient personServiceClient;

    @Spy
    @InjectMocks
    private UserServiceImpl userServiceTest;

    //////////////////////////// testing getUserInfo() method /////////////////////////

    @Test
    @DisplayName("Test get user information functionality")
    public void givenExchange_whenGetUserInfo_thenReturnUserInfo() {

        //given
        String token = "eyJhbGciOiJSUzI1NiIsInR5cCIgOiAiSldUIiwia2lkIiA6ICJjVDRQNWh4V05hV1JKM0t3QlBCbmtGa3JHdGVFdHYxeks1bGdsUzV0dkdjIn0.eyJleHAiOjE3NDIyMzc4MDAsImlhdCI6MTc0MjIzNzUwMCwianRpIjoiOWQ1NDNhZTktMWMxYS00Zjg0LWFmMWItZDVmN2YxMDM2M2YwIiwiaXNzIjoiaHR0cDovL2xvY2FsaG9zdDo4MDgyL3JlYWxtcy9teXJlYWxtIiwiYXVkIjoiYWNjb3VudCIsInN1YiI6IjVhYjIzNTg3LWJlNDQtNDI5Zi1hY2E5LWRlMzJiNDZkZjQ3YyIsInR5cCI6IkJlYXJlciIsImF6cCI6ImtleWNsb2FrLWNsaWVudCIsInNpZCI6IjI0MjljZTVhLWYyZTgtNDViNC04NzZlLWIyOTJkZWRmNGU5MyIsImFjciI6IjEiLCJhbGxvd2VkLW9yaWdpbnMiOlsiaHR0cDovL2xvY2FsaG9zdDo4MDgxIl0sInJlYWxtX2FjY2VzcyI6eyJyb2xlcyI6WyJkZWZhdWx0LXJvbGVzLW15cmVhbG0iLCJvZmZsaW5lX2FjY2VzcyIsInVtYV9hdXRob3JpemF0aW9uIl19LCJyZXNvdXJjZV9hY2Nlc3MiOnsiYWNjb3VudCI6eyJyb2xlcyI6WyJtYW5hZ2UtYWNjb3VudCIsIm1hbmFnZS1hY2NvdW50LWxpbmtzIiwidmlldy1wcm9maWxlIl19fSwic2NvcGUiOiJlbWFpbCBwcm9maWxlIiwiZW1haWxfdmVyaWZpZWQiOmZhbHNlLCJuYW1lIjoiRmlyc3RUZXN0IExhc3RUZXN0IiwiR3VpZCI6ImRhNDMxYjMzIiwicHJlZmVycmVkX3VzZXJuYW1lIjoidGVzdCIsImdpdmVuX25hbWUiOiJGaXJzdFRlc3QiLCJmYW1pbHlfbmFtZSI6Ikxhc3RUZXN0IiwiZW1haWwiOiJrY2FfMzNAbWFpbC5ydSJ9.ZqMrGaRua4sl0dZxSU_fLO1TTP6nK5r0WsbSMJfLbA_G3xFFqPhr5x9bBXpKhLbQwVw8sedvtcuVk3yxAmPal5qlG7QLfa2jGhP_S2qMD8g2CJu22tTd4jsfSc9FHJvmPs2kiXc6qmSLGK3Y06TyNUaNybDiFAj7Oonk6DR9HqF9IJAPGaPoc25MDCZNE-XUnWdL9YqOCRNhV0QAklggRFD1EwzpmLw2wDlu7j1P8Mgm1Jue63_-dVEeonyem63f1zj2IGaKgtKGVmKtuOS3tarY2iUBE4GAOm_8PZTgNGbpWpy1zlMcSwAXcCnFbhBkdYQspKKQYANQeB29PM09cw";
        MockServerWebExchange exchange = MockServerWebExchange
                .from(MockServerHttpRequest
                        .get("https://localhost:8081/v1/me")
                        .header("Authorization", "Bearer " + token)
                        .build()
                );
        IndividualResponseDTO individualResponseDTO = DataUtils.getIndividualResponseDto();

        BDDMockito.given(personServiceClient.getUser("da431b33")).willReturn(Mono.just(individualResponseDTO));

        //when
        Mono<IndividualResponseDTO> obtainedDto = userServiceTest.getUserInfo(exchange);

        //then
        StepVerifier.create(obtainedDto)
                .expectNext(individualResponseDTO)
                .verifyComplete();
        verify(personServiceClient, times(1)).getUser(anyString());
    }

    @Test
    @DisplayName("Test get user information with bad response from personServiceClient functionality")
    public void givenExchange_whenGetUserInfo_thenExceptionReturn() {

        //given
        String token = "eyJhbGciOiJSUzI1NiIsInR5cCIgOiAiSldUIiwia2lkIiA6ICJjVDRQNWh4V05hV1JKM0t3QlBCbmtGa3JHdGVFdHYxeks1bGdsUzV0dkdjIn0.eyJleHAiOjE3NDIyMzc4MDAsImlhdCI6MTc0MjIzNzUwMCwianRpIjoiOWQ1NDNhZTktMWMxYS00Zjg0LWFmMWItZDVmN2YxMDM2M2YwIiwiaXNzIjoiaHR0cDovL2xvY2FsaG9zdDo4MDgyL3JlYWxtcy9teXJlYWxtIiwiYXVkIjoiYWNjb3VudCIsInN1YiI6IjVhYjIzNTg3LWJlNDQtNDI5Zi1hY2E5LWRlMzJiNDZkZjQ3YyIsInR5cCI6IkJlYXJlciIsImF6cCI6ImtleWNsb2FrLWNsaWVudCIsInNpZCI6IjI0MjljZTVhLWYyZTgtNDViNC04NzZlLWIyOTJkZWRmNGU5MyIsImFjciI6IjEiLCJhbGxvd2VkLW9yaWdpbnMiOlsiaHR0cDovL2xvY2FsaG9zdDo4MDgxIl0sInJlYWxtX2FjY2VzcyI6eyJyb2xlcyI6WyJkZWZhdWx0LXJvbGVzLW15cmVhbG0iLCJvZmZsaW5lX2FjY2VzcyIsInVtYV9hdXRob3JpemF0aW9uIl19LCJyZXNvdXJjZV9hY2Nlc3MiOnsiYWNjb3VudCI6eyJyb2xlcyI6WyJtYW5hZ2UtYWNjb3VudCIsIm1hbmFnZS1hY2NvdW50LWxpbmtzIiwidmlldy1wcm9maWxlIl19fSwic2NvcGUiOiJlbWFpbCBwcm9maWxlIiwiZW1haWxfdmVyaWZpZWQiOmZhbHNlLCJuYW1lIjoiRmlyc3RUZXN0IExhc3RUZXN0IiwiR3VpZCI6ImRhNDMxYjMzIiwicHJlZmVycmVkX3VzZXJuYW1lIjoidGVzdCIsImdpdmVuX25hbWUiOiJGaXJzdFRlc3QiLCJmYW1pbHlfbmFtZSI6Ikxhc3RUZXN0IiwiZW1haWwiOiJrY2FfMzNAbWFpbC5ydSJ9.ZqMrGaRua4sl0dZxSU_fLO1TTP6nK5r0WsbSMJfLbA_G3xFFqPhr5x9bBXpKhLbQwVw8sedvtcuVk3yxAmPal5qlG7QLfa2jGhP_S2qMD8g2CJu22tTd4jsfSc9FHJvmPs2kiXc6qmSLGK3Y06TyNUaNybDiFAj7Oonk6DR9HqF9IJAPGaPoc25MDCZNE-XUnWdL9YqOCRNhV0QAklggRFD1EwzpmLw2wDlu7j1P8Mgm1Jue63_-dVEeonyem63f1zj2IGaKgtKGVmKtuOS3tarY2iUBE4GAOm_8PZTgNGbpWpy1zlMcSwAXcCnFbhBkdYQspKKQYANQeB29PM09cw";
        MockServerWebExchange exchange = MockServerWebExchange
                .from(MockServerHttpRequest
                        .get("https://localhost:8081/v1/me")
                        .header("Authorization", "Bearer " + token)
                        .build()
                );
        BDDMockito.given(personServiceClient.getUser(anyString())).willReturn(Mono.error(new UserNotFoundException("Something went wrong", "USER_NOT_FOUND")));

        //when
        Mono<IndividualResponseDTO> obtainedDto = userServiceTest.getUserInfo(exchange);

        //then
        StepVerifier.create(obtainedDto)
                .expectError(UserNotFoundException.class)
                .verify();
        verify(personServiceClient, times(1)).getUser("da431b33");
    }

    @Test
    @DisplayName("Test get user information with incorrect exchange functionality")
    public void givenIncorrectExchange_whenGetUserInfo_thenExceptionReturn() {

        //given
        String token = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiYWRtaW4iOnRydWUsImlhdCI6MTUxNjIzOTAyMn0.KMUFsIDTnFmyG3nMiGM6H9FNFUROf3wh7SmqJp-QV30";
        MockServerWebExchange exchange = MockServerWebExchange
                .from(MockServerHttpRequest
                        .get("https://localhost:8081/v1/me")
                        .header("Authorization", "Bearer " + token)
                        .build()
                );

        //when
        Mono<IndividualResponseDTO> obtainedDto = userServiceTest.getUserInfo(exchange);

        //then
        StepVerifier.create(obtainedDto)
                .expectError(UserNotFoundException.class)
                .verify();
        verify(personServiceClient, times(0)).getUser(anyString());
    }


    //////////////////////////// testing login() method /////////////////////////////

    @Test
    @DisplayName("Test login user by username and password functionality")
    public void givenUsernameAndPassword_whenLoginUser_thenReturnAccessTokenDTO() throws JsonProcessingException {

        //given
        AccessTokenDTO accessTokenDTO = DataUtils.getAccessTokenDTO();

        BDDMockito.given(keycloakClient.login(anyString(), anyString())).willReturn(Mono.just(accessTokenDTO));

        //when
        Mono<AccessTokenDTO> obtainedDto = userServiceTest.login("username", "password");

        //then
        StepVerifier.create(obtainedDto)
                .expectNext(accessTokenDTO)
                .verifyComplete();
        verify(keycloakClient, times(1)).login(anyString(), anyString());
    }

    @Test
    @DisplayName("Test login user functionality when keycloakService return Exception")
    public void givenUsernameAndPassword_whenLoginUser_thenReturnException() throws JsonProcessingException {

        //given
        BDDMockito.given(keycloakClient.login(anyString(), anyString()))
                .willReturn(Mono.error(new LoginFailedException("Failed to login, check your credentials", "LOGIN_FAILED")));

        //when
        Mono<AccessTokenDTO> obtainedToken = userServiceTest.login("username", "password");

        //then
        StepVerifier.create(obtainedToken)
                .expectError(LoginFailedException.class)
                .verify();
        verify(keycloakClient, times(1)).login(anyString(), anyString());
    }


    //////////////////////////// testing registerUser() method /////////////////////////////

    @Test
    @DisplayName("Test register user by provided UserRegistrationDto functionality")
    public void givenUserRegistrationDto_whenRegisterUser_thenReturnAccessTokenDTO() {

        //given
        IndividualResponseDTO individualResponseDTO = DataUtils.getIndividualResponseDto();
        UserRegistrationDTO userRegistrationDTO = DataUtils.getUserRegistrationDTO();
        ServerResponse response =DataUtils.getServerResponse201();
        AccessTokenDTO accessTokenDTO = DataUtils.getAccessTokenDTO();

        BDDMockito.given(personServiceClient.registerUser(any(UserRegistrationDTO.class)))
                .willReturn(Mono.just(individualResponseDTO));
        BDDMockito.given(keycloakClient.addUser(any(KeycloakUserDTO.class))).willReturn(Mono.just(response));
        BDDMockito.given(keycloakClient.login(anyString(),anyString())).willReturn(Mono.just(accessTokenDTO));

        //when
        Mono<AccessTokenDTO> obtainedToken = userServiceTest.registerUser(userRegistrationDTO);

        //then
        StepVerifier.create(obtainedToken)
                .expectNext(accessTokenDTO)
                .verifyComplete();
        verify(personServiceClient, times(1)).registerUser(any(UserRegistrationDTO.class));
        verify(keycloakClient, times(1)).addUser(any(KeycloakUserDTO.class));
        verify(keycloakClient, times(1)).login(anyString(), anyString());
    }

    @Test
    @DisplayName("Test register user by provided UserRegistrationDto with incorrect passwords functionality")
    public void givenIncorrectUserRegistrationDto_whenRegisterUser_thenReturnException() {

        //given
        UserRegistrationDTO userRegistrationDTO = DataUtils.getUserRegistrationDTO()
                .toBuilder()
                .confirmedPassword("pass")
                .build();

        //when
        Mono<AccessTokenDTO> obtainedToken = userServiceTest.registerUser(userRegistrationDTO);

        //then
        StepVerifier.create(obtainedToken)
                .expectError(RegistrationFailedException.class)
                .verify();
        verify(personServiceClient, times(0)).registerUser(any(UserRegistrationDTO.class));
        verify(keycloakClient, times(0)).addUser(any(KeycloakUserDTO.class));
        verify(keycloakClient, times(0)).login(anyString(), anyString());
    }

    @Test
    @DisplayName("Test register user functionality when personalServiceClient return Exception")
    public void givenUserRegistrationDto_whenRegisterUser_thenReturnPersonServiceClientException() {

        //given
        UserRegistrationDTO userRegistrationDTO = DataUtils.getUserRegistrationDTO();

        BDDMockito.given(personServiceClient.registerUser(any(UserRegistrationDTO.class)))
                .willReturn(Mono.error(new RegistrationFailedException("Something went wrong", "REGISTRATION_FAILED")));

        //when
        Mono<AccessTokenDTO> obtainedToken = userServiceTest.registerUser(userRegistrationDTO);

        //then
        StepVerifier.create(obtainedToken)
                .expectError(RegistrationFailedException.class)
                .verify();
        verify(personServiceClient, times(1)).registerUser(any(UserRegistrationDTO.class));
        verify(keycloakClient, times(0)).addUser(any(KeycloakUserDTO.class));
        verify(keycloakClient, times(0)).login(anyString(), anyString());
    }

    @Test
    @DisplayName("Test register user functionality when keycloakClient.addUser() return Exception")
    public void givenUserRegistrationDto_whenRegisterUser_thenReturnKeycloakClientException() {

        //given
        IndividualResponseDTO individualResponseDTO = DataUtils.getIndividualResponseDto();
        UserRegistrationDTO userRegistrationDTO = DataUtils.getUserRegistrationDTO();
        ServerResponse response =DataUtils.getServerResponse404();

        BDDMockito.given(personServiceClient.registerUser(any(UserRegistrationDTO.class)))
                .willReturn(Mono.just(individualResponseDTO));
        BDDMockito.given(personServiceClient.deleteUser(anyString()))
                .willReturn(Mono.empty());
        BDDMockito.given(keycloakClient.addUser(any(KeycloakUserDTO.class))).willReturn(Mono.just(response));

        //when
        Mono<AccessTokenDTO> obtainedToken = userServiceTest.registerUser(userRegistrationDTO);

        //then
        StepVerifier.create(obtainedToken)
                .expectError(RegistrationFailedException.class)
                .verify();
        verify(personServiceClient, times(1)).registerUser(any(UserRegistrationDTO.class));
        verify(personServiceClient, times(1)).deleteUser(anyString());
        verify(keycloakClient, times(1)).addUser(any(KeycloakUserDTO.class));
        verify(keycloakClient, times(0)).login(anyString(), anyString());
    }

    @Test
    @DisplayName("Test register user by provided UserRegistrationDto functionality")
    public void givenUserRegistrationDto_whenRegisterUser_thenReturnKeycloakClientLoginException() {

        //given
        IndividualResponseDTO individualResponseDTO = DataUtils.getIndividualResponseDto();
        UserRegistrationDTO userRegistrationDTO = DataUtils.getUserRegistrationDTO();
        ServerResponse response =DataUtils.getServerResponse201();


        BDDMockito.given(personServiceClient.registerUser(any(UserRegistrationDTO.class)))
                .willReturn(Mono.just(individualResponseDTO));
        BDDMockito.given(keycloakClient.addUser(any(KeycloakUserDTO.class))).willReturn(Mono.just(response));
        BDDMockito.given(keycloakClient.login(anyString(),anyString()))
                .willReturn(Mono.error(new LoginFailedException("Failed to login", "LOGIN_FAILED")));

        //when
        Mono<AccessTokenDTO> obtainedToken = userServiceTest.registerUser(userRegistrationDTO);

        //then
        StepVerifier.create(obtainedToken)
                .expectError(LoginFailedException.class)
                .verify();
        verify(personServiceClient, times(1)).registerUser(any(UserRegistrationDTO.class));
        verify(keycloakClient, times(1)).addUser(any(KeycloakUserDTO.class));
        verify(keycloakClient, times(1)).login(anyString(), anyString());
    }

}
