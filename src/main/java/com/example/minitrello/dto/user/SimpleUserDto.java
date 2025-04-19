package com.example.minitrello.dto.user;

import com.example.minitrello.model.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Data Transfer Object for User entity.
 * Used for transferring user data to and from the client without exposing sensitive information.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SimpleUserDto {

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
     * User's role in the system.
     */
    private Role role;

}