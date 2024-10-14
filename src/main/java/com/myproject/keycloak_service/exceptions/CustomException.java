package com.myproject.keycloak_service.exceptions;

import lombok.Getter;

@Getter
public class CustomException extends RuntimeException{
    protected String errorCode;

    public CustomException(String message, String errorCode){
        super(message);
        this.errorCode = errorCode;
    }
}
