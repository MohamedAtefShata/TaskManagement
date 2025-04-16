package com.example.minitrello.controller;

import com.example.minitrello.dto.user.UserDto;
import com.example.minitrello.model.User;
import com.example.minitrello.service.interfaces.AuthService;
import com.example.minitrello.service.interfaces.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Controller for managing user-related operations.
 * Provides APIs for fetching, updating, and deleting user information.
 */
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Users", description = "User management APIs")
@SecurityRequirement(name = "bearerAuth")
public class UserController {

    private final UserService userService;
    private final AuthService authService;

    /**
     * Retrieves the profile of the currently authenticated user.
     *
     * @return ResponseEntity containing the user profile
     */
    @GetMapping("/me")
    @Operation(summary = "Get current user profile", description = "Retrieves the profile of the currently authenticated user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Profile retrieved successfully",
                    content = @Content(schema = @Schema(implementation = User.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<User> getCurrentUserProfile() {
        log.debug("Fetching current user profile");
        User currentUser = authService.getCurrentAuthenticatedUser();
        return ResponseEntity.ok(currentUser);
    }

    /**
     * Updates the profile of the currently authenticated user.
     *
     * @param userDto DTO containing user information to update
     * @return ResponseEntity containing the updated user profile
     */
    @PutMapping("/me")
    @Operation(summary = "Update current user profile", description = "Updates the profile of the currently authenticated user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Profile updated successfully",
                    content = @Content(schema = @Schema(implementation = User.class))),
            @ApiResponse(responseCode = "400", description = "Invalid input"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<User> updateCurrentUserProfile(@Valid @RequestBody UserDto userDto) {
        log.info("Updating current user profile");
        User currentUser = authService.getCurrentAuthenticatedUser();

        // Map DTO fields to User entity
        User userToUpdate = new User();
        userToUpdate.setName(userDto.getName());

        User updatedUser = userService.updateUser(currentUser.getId(), userToUpdate);
        return ResponseEntity.ok(updatedUser);
    }

    /**
     * Retrieves a specific user by ID.
     * Only accessible to users with ADMIN role.
     *
     * @param id ID of the user to retrieve
     * @return ResponseEntity containing the user information
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get user by ID", description = "Retrieves a specific user by their ID (admin only)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User retrieved successfully",
                    content = @Content(schema = @Schema(implementation = User.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden - requires admin role"),
            @ApiResponse(responseCode = "404", description = "User not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<User> getUserById(
            @Parameter(description = "User ID", required = true) @PathVariable Long id) {
        log.debug("Fetching user with ID: {}", id);
        return userService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Retrieves a paginated list of all users.
     * Only accessible to users with ADMIN role.
     *
     * @param pageable pagination information
     * @return ResponseEntity containing a page of users
     */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get all users", description = "Retrieves a paginated list of all users (admin only)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Users retrieved successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden - requires admin role"),
            @ApiResponse(responseCode = "500", description = "Internal server error1")
    })
    public ResponseEntity<Page<User>> getAllUsers(Pageable pageable) {
        log.info("Finding all users with pagination");
        log.debug("Fetching all users with pagination");
        Page<User> users = userService.findAllUsers(pageable);
        return ResponseEntity.ok(users);
    }
}