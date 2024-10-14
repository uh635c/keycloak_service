package com.myproject.keycloak_service.exceptions;

public class LoginFailedException extends CustomException{
    public LoginFailedException(String message, String errorCode) {
        super(message, errorCode);
    }
}
