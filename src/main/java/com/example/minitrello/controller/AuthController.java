package com.example.minitrello.controller;

import com.example.minitrello.dto.auth.LoginRequest;
import com.example.minitrello.dto.auth.LoginResponse;
import com.example.minitrello.dto.auth.RegisterRequest;
import com.example.minitrello.dto.user.UserDto;
import com.example.minitrello.service.interfaces.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Controller for handling authentication-related endpoints.
 * Provides APIs for user registration and login.
 * Only handles DTOs for request/response, never entities.
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Authentication", description = "Authentication management APIs")
public class AuthController {

    private final AuthService authService;

    /**
     * Registers a new user in the system.
     *
     * @param registerRequest DTO containing registration details
     * @return ResponseEntity containing the created user information as DTO
     */
    @PostMapping("/register")
    @Operation(summary = "Register a new user", description = "Creates a new user account with USER role")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "User registered successfully",
                    content = @Content(schema = @Schema(implementation = UserDto.class))),
            @ApiResponse(responseCode = "400", ref = "BadRequest"),
            @ApiResponse(responseCode = "500", ref = "ServerError")
    })
    public ResponseEntity<UserDto> registerUser(@Valid @RequestBody RegisterRequest registerRequest) {
        log.info("Received registration request for email: {}", registerRequest.getEmail());
        UserDto userDto = authService.registerUser(registerRequest);
        return new ResponseEntity<>(userDto, HttpStatus.CREATED);
    }

    /**
     * Authenticates a user and returns a JWT token.
     *
     * @param loginRequest DTO containing login credentials
     * @return ResponseEntity containing the JWT token and user information
     */
    @PostMapping("/login")
    @Operation(summary = "Authenticate user", description = "Validates credentials and returns JWT token")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Authentication successful",
                    content = @Content(schema = @Schema(implementation = LoginResponse.class))),
            @ApiResponse(responseCode = "401", ref = "Unauthorized"),
            @ApiResponse(responseCode = "500", ref = "ServerError")
    })
    public ResponseEntity<LoginResponse> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) {
        log.info("Received login request for email: {}", loginRequest.getEmail());
        LoginResponse loginResponse = authService.authenticateUser(loginRequest);
        return ResponseEntity.ok(loginResponse);
    }

    /**
     * Validates a JWT token.
     *
     * @param token JWT token to validate
     * @return ResponseEntity indicating if the token is valid
     */
    @GetMapping("/validate")
    @Operation(summary = "Validate JWT token", description = "Checks if the provided token is valid")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Token is valid"),
            @ApiResponse(responseCode = "400", ref = "BadRequest"),
            @ApiResponse(responseCode = "500", ref = "ServerError")
    })
    public ResponseEntity<Boolean> validateToken(@RequestParam String token) {
        log.debug("Received token validation request");
        boolean isValid = authService.validateToken(token);
        return ResponseEntity.ok(isValid);
    }
}