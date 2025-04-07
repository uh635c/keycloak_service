package com.myproject.keycloak_service.it;

import com.myproject.keycloak_service.client.KeycloakClient;
import com.myproject.keycloak_service.client.PersonServiceClient;
import com.myproject.keycloak_service.config.KeycloakConfig;
import com.myproject.keycloak_service.config.KeycloakTestContainerConfig;
import com.myproject.keycloak_service.dto.AccessTokenDTO;
import com.myproject.keycloak_service.dto.LoginDTO;
import com.myproject.keycloak_service.service.UserService;
import com.myproject.keycloak_service.utils.DataUtils;
import org.hamcrest.core.Is;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.mockserver.client.MockServerClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.testcontainers.containers.MockServerContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;
import reactor.core.publisher.Mono;
import ru.uh635c.dto.IndividualResponseDTO;
import ru.uh635c.dto.UserRegistrationDTO;

import java.time.Duration;

import static org.testcontainers.utility.MountableFile.forClasspathResource;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@AutoConfigureWebTestClient
@Import({KeycloakTestContainerConfig.class, KeycloakConfig.class})
@TestInstance(TestInstance.Lifecycle.PER_METHOD)
@Testcontainers
public class ItUserControllerV1Tests {

    @Autowired
    WebTestClient webTestClient;
    @Autowired
    UserService userService;
    @Autowired
    static KeycloakClient keycloakClient;
    @Autowired
    PersonServiceClient personServiceClient;

    @Container
    static MockServerContainer container = new MockServerContainer(DockerImageName.parse("mockserver/mockserver:5.15.0"))
            .withEnv("MOCKSERVER_WATCH_INITIALIZATION_JSON", "true")
            .withEnv("MOCKSERVER_INITIALIZATION_JSON_PATH", "expectations.json")
            .withCopyFileToContainer(forClasspathResource("expectations.json"), "/home/nonroot/./expectations.json");

    static MockServerClient mockServerClient;

    @DynamicPropertySource
    static void overrideProperties(DynamicPropertyRegistry registry) {
        mockServerClient = new MockServerClient(container.getHost(), container.getServerPort());
        registry.add("uri.person-service.baseURL", () -> container.getEndpoint());
    }

    @BeforeEach
    public void setUp() {
        webTestClient = webTestClient.mutate()
                .responseTimeout(Duration.ofMillis(500000000))
                .build();
    }

    @Test
    @DisplayName("Test successful get user information functionality")
    public void givenGetMe_whenGetUserData_thenOReturnedIndividualResponseDto() {
        //given
        UserRegistrationDTO userRegistrationDTO = DataUtils.getUserRegistrationDTO()
                .toBuilder()
                .email("success_get_user@mail.com")
                .build();
        String token = userService.registerUser(userRegistrationDTO).map(AccessTokenDTO::accessToken).block();

        //when
        WebTestClient.ResponseSpec result = webTestClient.get()
                .uri("/v1/me")
                .header("Authorization", "Bearer " + token)
                .exchange();

        //then
        result.expectStatus().isOk()
                .expectBody(IndividualResponseDTO.class)
                .value(Is.isA(IndividualResponseDTO.class))
                .consumeWith(System.out::println);

    }

    @Test
    @DisplayName("Test get user information functionality when PersonServiceClient return exception")
    public void givenGetMe_whenGetUserData_thenReturnedExceptionFromPersonClient() {
        //given
        UserRegistrationDTO userRegistrationDTO = DataUtils.getUserRegistrationDTO()
                .toBuilder()
                .email("incorrect_guid@mail.com")
                .build();

        String token = userService.registerUser(userRegistrationDTO).map(AccessTokenDTO::accessToken).block();

        //when
        WebTestClient.ResponseSpec result = webTestClient.get()
                .uri("/v1/me")
                .header("Authorization", "Bearer " + token)
                .exchange();

        //then
        result.expectStatus().isBadRequest()
                .expectBody()
                .consumeWith(System.out::println)
                .jsonPath("$.error_message").isEqualTo("Something went wrong, user information not found");

    }

    @Test
    @DisplayName("Test get user information functionality when token does not contain guid")
    public void givenGetMe_whenGetUserDataAndIncorrectToken_thenReturnedException() {
        //given
        UserRegistrationDTO userRegistrationDTO = DataUtils.getUserRegistrationDTO()
                .toBuilder()
                .email("no_guid@mail.com")
                .build();

        String token = userService.registerUser(userRegistrationDTO).map(AccessTokenDTO::accessToken).block();

        //when
        WebTestClient.ResponseSpec result = webTestClient.get()
                .uri("/v1/me")
                .header("Authorization", "Bearer " + token)
                .exchange();

        //then
        result.expectStatus().isBadRequest()
                .expectBody()
                .consumeWith(System.out::println)
                .jsonPath("$.error_message").isEqualTo("Something went wrong, guid not found");

    }

    @Test
    @DisplayName("Test successful login functionality")
    public void givenLoginDto_whenLogin_thenReturnedAccessTokenDto() {
        //given
        UserRegistrationDTO userRegistrationDTO = DataUtils.getUserRegistrationDTO()
                .toBuilder()
                .email("success_login@mail.com")
                .build();
        LoginDTO loginDTO = LoginDTO.builder()
                .userName(userRegistrationDTO.getEmail())
                .password(userRegistrationDTO.getPassword())
                .build();

        userService.registerUser(userRegistrationDTO).map(AccessTokenDTO::accessToken).block();

        //when
        WebTestClient.ResponseSpec result = webTestClient.post()
                .uri("/v1/login")
                .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                .body(Mono.just(loginDTO), LoginDTO.class)
                .exchange();

        //then
        result.expectStatus().isOk()
                .expectBody(AccessTokenDTO.class)
                .value(Is.isA(AccessTokenDTO.class))
                .consumeWith(System.out::println);

    }

    @Test
    @DisplayName("Test login failed functionality")
    public void givenIncorrectLoginDto_whenLogin_thenReturnedLoginFailedException() {
        //given
        UserRegistrationDTO userRegistrationDTO = DataUtils.getUserRegistrationDTO()
                .toBuilder()
                .email("failed_login@mail.com")
                .build();
        LoginDTO loginDTO = LoginDTO.builder()
                .userName(userRegistrationDTO.getEmail())
                .password("pass")
                .build();
        userService.registerUser(userRegistrationDTO).map(AccessTokenDTO::accessToken).block();

        //when
        WebTestClient.ResponseSpec result = webTestClient.post()
                .uri("/v1/login")
                .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                .body(Mono.just(loginDTO), LoginDTO.class)
                .exchange();

        //then
        result.expectStatus().isBadRequest()
                .expectBody()
                .consumeWith(System.out::println)
                .jsonPath("$.error_message").isEqualTo("Failed to login " + userRegistrationDTO.getEmail() + ", check your credentials");
    }


    @Test
    @DisplayName("Test successful user registration functionality")
    public void givenUserRegistrationDto_whenRegistration_thenReturnedAccessTokenDto() {
        //given
        UserRegistrationDTO userRegistrationDTO = DataUtils.getUserRegistrationDTO()
                .toBuilder()
                .email("success_registration@mail.com")
                .build();

        //when
        WebTestClient.ResponseSpec result = webTestClient.post()
                .uri("/v1/auth/registration")
                .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                .body(Mono.just(userRegistrationDTO), UserRegistrationDTO.class)
                .exchange();

        //then
        result.expectStatus().isOk()
                .expectBody(AccessTokenDTO.class)
                .value(Is.isA(AccessTokenDTO.class))
                .consumeWith(System.out::println);
    }

    @Test
    @DisplayName("Test unsuccessful user registration functionality because of PersonService Exception")
    public void givenUserRegistrationDto_whenRegistration_thenReturnedExceptionFromPersonService() {
        //given
        UserRegistrationDTO userRegistrationDTO = DataUtils.getUserRegistrationDTO()
                .toBuilder()
                .firstName("John")
                .build();

        //when
        WebTestClient.ResponseSpec result = webTestClient.post()
                .uri("/v1/auth/registration")
                .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                .body(Mono.just(userRegistrationDTO), LoginDTO.class)
                .exchange();

        //then
        result.expectStatus().isBadRequest()
                .expectBody()
                .consumeWith(System.out::println)
                .jsonPath("$.error_message").isEqualTo("Something went wrong, user not saved");
    }

    @Test
    @DisplayName("Test unsuccessful user registration functionality because of bad credentials")
    public void givenIncorrectUserRegistrationDto_whenRegistration_thenReturnedException() {
        //given
        UserRegistrationDTO userRegistrationDTO = DataUtils.getUserRegistrationDTO()
                .toBuilder()
                .confirmedPassword("pass")
                .build();


        //when
        WebTestClient.ResponseSpec result = webTestClient.post()
                .uri("/v1/auth/registration")
                .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                .body(Mono.just(userRegistrationDTO), LoginDTO.class)
                .exchange();

        //then
        result.expectStatus().isBadRequest()
                .expectBody()
                .consumeWith(System.out::println)
                .jsonPath("$.error_message").isEqualTo("Provided bad credentials");
    }

    @Test
    @DisplayName("Test unsuccessful user registration functionality because of KeycloakService Exception")
    public void givenUserRegistrationDto_whenRegistration_thenReturnedExceptionFromKeycloakService() {
        //given
        UserRegistrationDTO userRegistrationDTO = DataUtils.getUserRegistrationDTO()
                .toBuilder()
                .email("")
                .build();

        //when
        WebTestClient.ResponseSpec result = webTestClient.post()
                .uri("/v1/auth/registration")
                .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                .body(Mono.just(userRegistrationDTO), LoginDTO.class)
                .exchange();

        //then
        result.expectStatus().isBadRequest()
                .expectBody()
                .consumeWith(System.out::println)
                .jsonPath("$.error_message").isEqualTo("User is not registered, try later");
    }
}
