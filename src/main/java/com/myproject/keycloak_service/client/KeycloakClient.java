package com.myproject.keycloak_service.client;

import com.myproject.keycloak_service.dto.AccessTokenDTO;
import com.myproject.keycloak_service.dto.KeycloakUserDTO;
import com.myproject.keycloak_service.exceptions.LoginFailedException;
import jakarta.ws.rs.core.Response;
import lombok.RequiredArgsConstructor;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.resource.UsersResource;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
public class KeycloakClient {

    private final Keycloak keycloak;
    private final WebClient webClient;

    @Value("${keycloak.realm}")
    public String realm;
    @Value("${keycloak.client-id}")
    private String clientId;
    @Value("${keycloak.client-secret}")
    private String clientSecret;
    @Value("${uri.token}")
    private String tokenURI;

    public Mono<Response> addUser(KeycloakUserDTO userDTO){

        UserRepresentation user = new UserRepresentation();
        user.setUsername(userDTO.email());
        user.setFirstName(userDTO.firstName());
        user.setLastName(userDTO.lastName());
        user.setEmail(userDTO.email());
        user.singleAttribute("GUID", userDTO.guid());
        user.setCredentials(Collections.singletonList(createPasswordCredentials(userDTO.password())));
        user.setEnabled(true);

        return Mono.just(keycloak.realm(realm).users().create(user));
    }

    public Mono<AccessTokenDTO> login(String username, String password){
        MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
        formData.add("username", username);
        formData.add("password", password);
        formData.add("grant_type", "password");
        formData.add("client_id", clientId);
        formData.add("client_secret", clientSecret);

        return webClient.post()
                .uri(tokenURI)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(BodyInserters.fromFormData(formData))
                .retrieve()
                .bodyToMono(AccessTokenDTO.class)
                .onErrorResume(e -> Mono.error(new LoginFailedException("Failed to login " + username + ", check your credentials", "LOGIN_FAILED")));

    }


//    --------- PRIVATE METHODS -------------

    private CredentialRepresentation createPasswordCredentials(String password) {
        CredentialRepresentation passwordCredentials = new CredentialRepresentation();
        passwordCredentials.setTemporary(false);
        passwordCredentials.setType(CredentialRepresentation.PASSWORD);
        passwordCredentials.setValue(password);
        return passwordCredentials;
    }


}
