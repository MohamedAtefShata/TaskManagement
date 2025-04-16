package com.example.minitrello.dto.auth;

import com.example.minitrello.model.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Data Transfer Object for login response.
 * Contains the JWT token and user information returned after successful authentication.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoginResponse {

    /**
     * JWT token to be used for authenticated requests.
     */
    private String token;

    /**
     * Type of token, typically "Bearer".
     */
    private String tokenType;

    /**
     * User's unique identifier.
     */
    private Long id;

    /**
     * User's display name.
     */
    private String name;

    /**
     * User's email address.
     */
    private String email;

    /**
     * User's role in the system (USER, ADMIN).
     */
    private Role role;
}