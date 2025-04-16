package com.example.minitrello.security;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

/**
 * Helper component for custom security expressions used in @PreAuthorize annotations.
 * This class provides methods that can be referenced in SpEL expressions.
 */
@Component("userSecurity")
@RequiredArgsConstructor
public class UserSecurity {

    /**
     * Checks if the authenticated user is the same as the user with the given ID.
     * Used in security expressions to allow users to access or modify only their own resources.
     *
     * @param userId the ID of the user to check against
     * @return true if the current user has the same ID, false otherwise
     */
    public boolean isCurrentUser(Long userId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            return false;
        }

        // Get the authenticated user's ID
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        Long authenticatedUserId = userDetails.getId();

        // Check if the authenticated user's ID matches the requested user ID
        return authenticatedUserId.equals(userId);
    }
}