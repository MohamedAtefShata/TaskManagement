package com.example.minitrello.service.interfaces;

import com.example.minitrello.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;

import java.util.Optional;

/**
 * Service interface for managing user-related operations.
 * Provides methods for CRUD operations on users and finding users by various criteria.
 * Security constraints are defined at the interface level.
 */
public interface UserService {

    /**
     * Creates a new user.
     *
     * @param user the user entity to be created
     * @return the created user
     */
    User createUser(User user);

    /**
     * Updates an existing user.
     * Users can only update their own profiles unless they have admin role.
     *
     * @param id the ID of the user to update
     * @param userDetails the user details to update
     * @return the updated user
     */
    @PreAuthorize("@userSecurity.isCurrentUser(#id) or hasRole('ADMIN')")
    User updateUser(Long id, User userDetails);

    /**
     * Finds a user by ID.
     * Regular users can only access their own profiles, admins can access any profile.
     *
     * @param id the ID of the user to find
     * @return an Optional containing the found user, or empty if not found
     */
    @PreAuthorize("@userSecurity.isCurrentUser(#id) or hasRole('ADMIN')")
    Optional<User> findById(Long id);

    /**
     * Finds a user by email.
     *
     * @param email the email of the user to find
     * @return an Optional containing the found user, or empty if not found
     */
    Optional<User> findByEmail(String email);

    /**
     * Retrieves all users with pagination.
     * Only administrators can access this method.
     *
     * @param pageable pagination information
     * @return a Page of users
     */
    @PreAuthorize("hasRole('ADMIN')")
    Page<User> findAllUsers(Pageable pageable);

    /**
     * Deletes a user by ID.
     * Only administrators can delete user accounts.
     *
     * @param id the ID of the user to delete
     * @return true if the user was deleted, false if the user was not found
     */
    @PreAuthorize("hasRole('ADMIN')")
    boolean deleteUser(Long id);

    /**
     * Checks if a user with the given email exists.
     *
     * @param email the email to check
     * @return true if a user with the email exists, false otherwise
     */
    boolean existsByEmail(String email);
}