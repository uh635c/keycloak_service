package com.myproject.keycloak_service.exceptions;

public class RegistrationFailedException extends CustomException{
    public RegistrationFailedException(String message, String errorCode) {
        super(message, errorCode);
    }
}
