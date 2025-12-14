package com.bankflow.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.servers.Server;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(
        info = @Info(
                title = "BankFlow API",
                version = "1.0",
                description = "Banking service API for managing customers, accounts, and transactions",
                contact = @Contact(
                        name = "BankFlow Support",
                        email = "support@bankflow.com"
                )
        ),
        servers = {
                @Server(url = "http://localhost:8080/api", description = "Local Development"),
                @Server(url = "https://bankflow-service-574624095538.us-central1.run.app/api", description = "Development Server")
        }
)
@SecurityScheme(
        name = "bearerAuth",
        type = SecuritySchemeType.HTTP,
        scheme = "bearer",
        bearerFormat = "JWT",
        description = "JWT authentication token. Get it from the /v1/auth/login endpoint."
)
public class OpenApiConfig {
}

