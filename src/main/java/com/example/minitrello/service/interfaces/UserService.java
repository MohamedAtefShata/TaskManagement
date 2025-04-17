package com.example.minitrello.service.interfaces;

import com.example.minitrello.dto.user.UserDto;
import com.example.minitrello.dto.user.UserUpdateDto;
import com.example.minitrello.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;

import java.util.Optional;

/**
 * Service interface for managing user-related operations.
 * Provides methods for CRUD operations on users and finding users by various criteria.
 * Returns DTOs to controllers for all operations.
 */
public interface UserService {

    /**
     * Creates a new user.
     *
     * @param user the user entity to be created
     * @return the created user entity (for internal service use)
     */
    User createUser(User user);

    /**
     * Updates an existing user.
     * Users can only update their own profiles unless they have admin role.
     *
     * @param id the ID of the user to update
     * @param updateDto the DTO containing fields to update
     * @return the updated user as DTO
     */
    @PreAuthorize("@userSecurity.isCurrentUser(#id) or hasRole('ADMIN')")
    UserDto updateUser(Long id, UserUpdateDto updateDto);

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
     * Finds a user by ID and returns as DTO.
     * Regular users can only access their own profiles, admins can access any profile.
     *
     * @param id the ID of the user to find
     * @return an Optional containing the found user DTO, or empty if not found
     */
    @PreAuthorize("@userSecurity.isCurrentUser(#id) or hasRole('ADMIN')")
    Optional<UserDto> findDtoById(Long id);

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
     * @return a Page of user DTOs
     */
    @PreAuthorize("hasRole('ADMIN')")
    Page<UserDto> findAllUser(Pageable pageable);

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

    /**
     * Converts a User entity to a UserDto.
     *
     * @param user the user entity to convert
     * @return the user DTO
     */
    UserDto toDto(User user);
}