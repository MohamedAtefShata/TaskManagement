package com.example.minitrello.dto.user;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Data Transfer Object for updating user active status.
 * Used specifically for admin operations to enable/disable users.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserStatusUpdateDto {

    /**
     * Flag indicating if the user account should be active.
     * Required field for status update operations.
     */
    @NotNull(message = "Active status is required")
    private Boolean isActive;
}