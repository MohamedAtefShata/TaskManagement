package com.example.minitrello.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Map;

/**
 * Configuration for OpenAPI documentation.
 * Sets up JWT authentication for API documentation and provides API information.
 */
@Configuration
public class OpenApiConfig {

    /**
     * Creates an OpenAPI configuration with JWT security scheme.
     *
     * @return OpenAPI configuration bean
     */
    @Bean
    public OpenAPI customOpenAPI() {
        final String securitySchemeName = "bearerAuth";

        return new OpenAPI()
                .info(apiInfo())
                .addSecurityItem(new SecurityRequirement().addList(securitySchemeName))
                .components(new Components()
                        .addSecuritySchemes(securitySchemeName,
                                new SecurityScheme()
                                        .name(securitySchemeName)
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")
                                        .description("Enter JWT Bearer token in the format 'Bearer {token}'")
                        )
                        .responses(createStandardResponses())
                );
    }

    /**
     * Creates API information details for OpenAPI documentation.
     *
     * @return Info object containing API metadata
     */
    private Info apiInfo() {
        return new Info()
                .title("Mini Trello REST API")
                .description("REST API for task management application similar to Trello")
                .version("1.0.0")
                .contact(new Contact()
                        .name("API Support")
                        .url("https://example.com/support")
                        .email("support@example.com"))
                .license(new License()
                        .name("Apache 2.0")
                        .url("https://www.apache.org/licenses/LICENSE-2.0.html"));
    }

    /**
     * Creates standard API responses for common HTTP status codes.
     *
     * @return Map of response names to ApiResponse objects
     */
    private Map<String, ApiResponse> createStandardResponses() {
        return Map.of(
                "BadRequest", new ApiResponse().description("Bad Request - Invalid input").content(new Content()),
                "Unauthorized", new ApiResponse().description("Unauthorized - Authentication required").content(new Content()),
                "Forbidden", new ApiResponse().description("Forbidden - Insufficient permissions").content(new Content()),
                "NotFound", new ApiResponse().description("Not Found - Resource not found").content(new Content()),
                "ServerError", new ApiResponse().description("Server Error - Internal server error").content(new Content())
        );
    }
}