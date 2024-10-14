package com.myproject.keycloak_service.errorhandling;

import org.springframework.boot.autoconfigure.web.WebProperties;
import org.springframework.boot.autoconfigure.web.reactive.error.AbstractErrorWebExceptionHandler;
import org.springframework.boot.web.error.ErrorAttributeOptions;
import org.springframework.boot.web.reactive.error.ErrorAttributes;
import org.springframework.context.ApplicationContext;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerCodecConfigurer;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.server.RequestPredicates;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;

import java.util.Map;

@Component
public class AppErrorWebExceptionHandler extends AbstractErrorWebExceptionHandler {
    public AppErrorWebExceptionHandler(ErrorAttributes errorAttributes,
                                       ApplicationContext applicationContext,
                                       ServerCodecConfigurer serverCodecConfigurer){

        super(errorAttributes, new WebProperties.Resources(), applicationContext);
        setMessageWriters(serverCodecConfigurer.getWriters());
        setMessageReaders(serverCodecConfigurer.getReaders());
    }

    @Override
    public RouterFunction<ServerResponse> getRoutingFunction(ErrorAttributes errorAttributes) {
         return RouterFunctions.route(RequestPredicates.all(), request -> {
             Map<String, Object> errorPropertiesMap = getErrorAttributes(request, ErrorAttributeOptions.defaults());

             return ServerResponse.status((HttpStatus) errorPropertiesMap.getOrDefault("status", 500))
                     .contentType(MediaType.APPLICATION_JSON)
                     .body(BodyInserters.fromValue(errorPropertiesMap.get("body")));
         });
    }
}
