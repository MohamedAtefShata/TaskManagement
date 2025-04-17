package com.example.minitrello.dto.user;

import com.example.minitrello.model.Role;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Data Transfer Object for updating user role.
 * Used specifically for admin operations.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserRoleUpdateDto {

    /**
     * The role to assign to the user.
     * Required field for role update operations.
     */
    @NotNull(message = "Role is required")
    private Role role;
}