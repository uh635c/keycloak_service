package com.myproject.keycloak_service.dto;

import lombok.Builder;

@Builder(toBuilder = true)
public record KeycloakUserDTO(
        String guid,
        String email,
        String firstName,
        String lastName,
        String password
) {}
