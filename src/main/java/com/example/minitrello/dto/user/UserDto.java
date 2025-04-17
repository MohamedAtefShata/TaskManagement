package com.example.minitrello.dto.user;

import com.example.minitrello.model.Role;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Data Transfer Object for User entity.
 * Used for transferring user data to and from the client without exposing sensitive information.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserDto {

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

    /**
     * Timestamp of when the user was created.
     */
    private LocalDateTime createdAt;

    /**
     * Timestamp of when the user was last updated.
     */
    private LocalDateTime updatedAt;

    /**
     * Number of tasks assigned to this user.
     * Only included in responses, not in requests.
     */
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private Integer assignedTaskCount;

    /**
     * Flag indicating if account is active.
     */
    private Boolean isActive;
}