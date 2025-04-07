package com.myproject.keycloak_service.client;

import com.myproject.keycloak_service.exceptions.RegistrationFailedException;
import com.myproject.keycloak_service.exceptions.UserNotFoundException;
import com.myproject.keycloak_service.mappers.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import ru.uh635c.dto.IndividualRequestDTO;
import ru.uh635c.dto.IndividualResponseDTO;
import ru.uh635c.dto.UserRegistrationDTO;

@Component
@RequiredArgsConstructor
public class PersonServiceClient {

    private final WebClient webClient;
    private final UserMapper userMapper;

    @Value("${uri.person-service.baseURL}")
    private String baseURL;
    @Value("${uri.person-service.individuals}")
    private String individualsPath;

    public Mono<IndividualResponseDTO> registerUser(UserRegistrationDTO userRegistrationDTO) {
        return webClient.post()
                .uri(baseURL + individualsPath)
                .contentType(MediaType.APPLICATION_JSON)
                .body(Mono.just(userMapper.map(userRegistrationDTO)), IndividualRequestDTO.class)
                .exchangeToMono(response -> {
                    if (response.statusCode().is2xxSuccessful()) {
                        return response.bodyToMono(IndividualResponseDTO.class);
                    }
                    return Mono.error(new RegistrationFailedException("Something went wrong, user not saved", "REGISTRATION_FAILED"));
                });
    }

    public Mono<IndividualResponseDTO> getUser(String id) {
        return webClient.get()
                .uri(baseURL + individualsPath + "/" + id)
                .exchangeToMono(response -> {
                    if (response.statusCode().is2xxSuccessful()) {
                        return response.bodyToMono(IndividualResponseDTO.class);
                    }
                    return Mono.error(new UserNotFoundException("Something went wrong, user information not found", "USER_NOT_FOUND"));
                });
    }

    public Mono<Void> deleteUser(String id) {
        return webClient.delete()
                .uri(baseURL + individualsPath + "/" + id)
                .retrieve()
                .bodyToMono(Void.class);
    }

}
