package com.crm.crm_backend.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.security.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration
public class OpenApiConfig {

    @Bean
    @Primary
    public OpenAPI crmOpenApi() {
        return new OpenAPI()
                .components(new Components()
                        // 1. Direct Bearer Token Input Field
                        .addSecuritySchemes("BearerToken", new SecurityScheme()
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                                .description("Paste access token directly here (without 'Bearer ' prefix)"))

                        // 2. Keycloak OAuth2 Login Flow (Client ID, Secret, Username, Password)
                        .addSecuritySchemes("KeycloakOAuth2", new SecurityScheme()
                                .type(SecurityScheme.Type.OAUTH2)
                                .flows(new OAuthFlows()
                                        .password(new OAuthFlow()
                                                .tokenUrl("http://localhost:8080/realms/erp/protocol/openid-connect/token")
                                        )
                                )
                        )
                )
                .addSecurityItem(new SecurityRequirement()
                        .addList("BearerToken")
                        .addList("KeycloakOAuth2"));
    }
}