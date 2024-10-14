package com.myproject.keycloak_service;

import org.springframework.boot.SpringApplication;

public class TestKeycloakServiceApplication {

	public static void main(String[] args) {
		SpringApplication.from(KeycloakServiceApplication::main).with(TestcontainersConfiguration.class).run(args);
	}

}
