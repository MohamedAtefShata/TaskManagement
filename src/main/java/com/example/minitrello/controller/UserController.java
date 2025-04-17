package com.example.minitrello.controller;

import com.example.minitrello.dto.user.UserDto;
import com.example.minitrello.dto.user.UserUpdateDto;
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

/**
 * Controller for managing user-related operations.
 * Provides APIs for fetching, updating, and deleting user information.
 * Only handles DTOs for request/response, never entities.
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
     * @return ResponseEntity containing the user profile DTO
     */
    @GetMapping("/me")
    @Operation(summary = "Get current user profile", description = "Retrieves the profile of the currently authenticated user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Profile retrieved successfully",
                    content = @Content(schema = @Schema(implementation = UserDto.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<UserDto> getCurrentUserProfile() {
        log.debug("Fetching current user profile");
        UserDto currentUser = authService.getCurrentAuthenticatedUserDto();
        return ResponseEntity.ok(currentUser);
    }

    /**
     * Updates the profile of the currently authenticated user.
     * Uses a dedicated DTO for update operations to control which fields can be modified.
     *
     * @param updateDto DTO containing user information to update
     * @return ResponseEntity containing the updated user profile DTO
     */
    @PutMapping("/me")
    @Operation(summary = "Update current user profile", description = "Updates the profile of the currently authenticated user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Profile updated successfully",
                    content = @Content(schema = @Schema(implementation = UserDto.class))),
            @ApiResponse(responseCode = "400", description = "Invalid input"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<UserDto> updateCurrentUserProfile(@Valid @RequestBody UserUpdateDto updateDto) {
        log.info("Updating current user profile");
        Long currentUserId = authService.getCurrentAuthenticatedUserId();
        UserDto updatedUser = userService.updateUser(currentUserId, updateDto);
        return ResponseEntity.ok(updatedUser);
    }

    /**
     * Retrieves a specific user by ID.
     * Only accessible to users with ADMIN role or the user themselves.
     *
     * @param id ID of the user to retrieve
     * @return ResponseEntity containing the user information DTO
     */
    @GetMapping("/{id}")
    @PreAuthorize("@userSecurity.isCurrentUser(#id) or hasRole('ADMIN')")
    @Operation(summary = "Get user by ID", description = "Retrieves a specific user by their ID (admin or self)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User retrieved successfully",
                    content = @Content(schema = @Schema(implementation = UserDto.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden - requires admin role or be the user"),
            @ApiResponse(responseCode = "404", description = "User not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<UserDto> getUserById(
            @Parameter(description = "User ID", required = true) @PathVariable Long id) {
        log.debug("Fetching user with ID: {}", id);
        return userService.findDtoById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Retrieves a paginated list of all users.
     * Only accessible to users with ADMIN role.
     *
     * @param pageable pagination information
     * @return ResponseEntity containing a page of user DTOs
     */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get all users", description = "Retrieves a paginated list of all users (admin only)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Users retrieved successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden - requires admin role"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<Page<UserDto>> getAllUsers(Pageable pageable) {
        log.debug("Fetching all users with pagination");
        Page<UserDto> users = userService.findAllUser(pageable);
        return ResponseEntity.ok(users);
    }
}