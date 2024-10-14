package com.myproject.keycloak_service.errorhandling;

import com.myproject.keycloak_service.dto.ErrorOutDTO;
import com.myproject.keycloak_service.exceptions.CustomException;
import org.springframework.boot.web.error.ErrorAttributeOptions;
import org.springframework.boot.web.reactive.error.DefaultErrorAttributes;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;

import java.util.HashMap;
import java.util.Map;

@Component
public class AppErrorAttributes extends DefaultErrorAttributes {

    @Override
    public Map<String, Object> getErrorAttributes(ServerRequest webRequest, ErrorAttributeOptions options) {
        Throwable error = getError(webRequest);
        Map<String, Object> errorAttributes = super.getErrorAttributes(webRequest, options);

        if(error instanceof CustomException){
            errorAttributes.put("status", HttpStatus.BAD_REQUEST);
            errorAttributes.put("body", ErrorOutDTO.builder()
                    .errorCode(((CustomException)error).getErrorCode())
                    .errorMessage(((CustomException)error).getMessage())
                    .build());
        }

        return errorAttributes;
    }
}
