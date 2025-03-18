package com.myproject.keycloak_service.service;

import com.myproject.keycloak_service.dto.KeycloakUserDTO;
import com.myproject.keycloak_service.dto.UserDTO;
import jakarta.ws.rs.core.Response;
import lombok.RequiredArgsConstructor;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.resource.UsersResource;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import ru.uh635c.dto.UserRegistrationDTO;

import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
public class KeycloakService {

    private final Keycloak keycloak;

    @Value("${keycloak.realm}")
    public String realm;

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

    public Mono<List<UserRepresentation>> getUser(String username){
        UsersResource usersResource = keycloak.realm(realm).users();
        return Mono.just(usersResource.searchByUsername(username, true));
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
