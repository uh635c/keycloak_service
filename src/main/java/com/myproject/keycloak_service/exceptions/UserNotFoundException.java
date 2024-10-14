package com.myproject.keycloak_service.exceptions;

public class UserNotFoundException extends CustomException{
    public UserNotFoundException(String message, String errorCode) {
        super(message, errorCode);
    }
}
