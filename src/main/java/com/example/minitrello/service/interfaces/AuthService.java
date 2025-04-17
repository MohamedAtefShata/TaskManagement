package com.example.minitrello.service.interfaces;

import com.example.minitrello.dto.auth.LoginRequest;
import com.example.minitrello.dto.auth.LoginResponse;
import com.example.minitrello.dto.auth.RegisterRequest;
import com.example.minitrello.dto.user.UserDto;
import org.springframework.security.access.prepost.PreAuthorize;

/**
 * Service interface for authentication operations.
 * Provides methods for user registration, login, and token validation.
 * Returns DTOs rather than entities to controllers.
 */
public interface AuthService {

    /**
     * Registers a new user.
     *
     * @param registerRequest the registration details
     * @return the newly created user DTO
     */
    UserDto registerUser(RegisterRequest registerRequest);

    /**
     * Authenticates a user and generates a JWT token.
     *
     * @param loginRequest the login credentials
     * @return a response containing the JWT token and user details
     */
    LoginResponse authenticateUser(LoginRequest loginRequest);

    /**
     * Validates if a JWT token is valid.
     *
     * @param token the JWT token to validate
     * @return true if the token is valid, false otherwise
     */
    boolean validateToken(String token);

    /**
     * Gets the authenticated user from the security context.
     * Requires authentication (any role).
     *
     * @return the currently authenticated user as DTO
     */
    @PreAuthorize("isAuthenticated()")
    UserDto getCurrentAuthenticatedUserDto();

    /**
     * Gets the ID of the authenticated user from the security context.
     * Requires authentication (any role).
     *
     * @return the currently authenticated user's ID
     */
    @PreAuthorize("isAuthenticated()")
    Long getCurrentAuthenticatedUserId();
}