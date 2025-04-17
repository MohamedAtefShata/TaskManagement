package com.example.minitrello.dto.user;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Data Transfer Object for updating user information.
 * Contains only fields that a regular user is allowed to update.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserUpdateDto {

    /**
     * User's display name.
     * Optional field for update operations.
     */
    @Size(min = 3, max = 100, message = "Name must be between 3 and 100 characters")
    private String name;

    /**
     * User's password.
     * Optional field for update operations. If null or empty, password won't be updated.
     */
    @Size(min = 6, max = 100, message = "Password must be between 6 and 100 characters")
    private String password;
}