package com.example.minitrello.service.interfaces;

import com.example.minitrello.dto.user.UserDto;
import com.example.minitrello.model.Role;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;

/**
 * Service interface for administrative operations.
 * Provides methods for admin-specific user management.
 * Returns DTOs to controllers for all operations.
 */
public interface AdminService {

    /**
     * Changes a user's role.
     * Only administrators can change user roles.
     *
     * @param userId the ID of the user whose role will be changed
     * @param role the new role to assign
     * @return the updated user as DTO
     */
    @PreAuthorize("hasRole('ADMIN')")
    UserDto changeUserRole(Long userId, Role role);

    /**
     * Gets all users with detailed information.
     * This is an admin-specific operation that may include more detailed information
     * than what's available to regular users.
     *
     * @param pageable pagination information
     * @return a Page of user DTOs with detailed information
     */
    @PreAuthorize("hasRole('ADMIN')")
    Page<UserDto> getAllUsersDetailed(Pageable pageable);

    /**
     * Disables a user account.
     * This is an admin-specific operation to temporarily prevent a user from accessing the system.
     *
     * @param userId the ID of the user to disable
     * @return the updated user as DTO
     */
    @PreAuthorize("hasRole('ADMIN')")
    UserDto disableUser(Long userId);

    /**
     * Enables a previously disabled user account.
     *
     * @param userId the ID of the user to enable
     * @return the updated user as DTO
     */
    @PreAuthorize("hasRole('ADMIN')")
    UserDto enableUser(Long userId);
}