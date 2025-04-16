package com.example.minitrello.controller;

import com.example.minitrello.model.Role;
import com.example.minitrello.model.User;
import com.example.minitrello.service.interfaces.AdminService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Controller for admin-only operations.
 * Provides APIs for user management tasks that require administrative privileges.
 */
@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@Slf4j
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Admin", description = "Admin-only APIs for user management")
@SecurityRequirement(name = "bearerAuth")
public class AdminController {

    private final AdminService adminService;

    /**
     * Retrieves detailed information about all users with pagination.
     *
     * @param pageable pagination information
     * @return ResponseEntity containing a page of users with detailed information
     */
    @GetMapping("/users")
    @Operation(summary = "Get all users with details", description = "Retrieves detailed information about all users (admin only)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Users retrieved successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden - requires admin role"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<Page<User>> getAllUsersDetailed(Pageable pageable) {
        log.info("Admin fetching detailed user information with pagination");
        Page<User> users = adminService.getAllUsersDetailed(pageable);
        return ResponseEntity.ok(users);
    }

    /**
     * Changes the role of a specific user.
     *
     * @param userId ID of the user whose role will be changed
     * @param roleMap Map containing the new role
     * @return ResponseEntity containing the updated user
     */
    @PutMapping("/users/{userId}/role")
    @Operation(summary = "Change user role", description = "Changes the role of a specific user (admin only)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Role changed successfully",
                    content = @Content(schema = @Schema(implementation = User.class))),
            @ApiResponse(responseCode = "400", description = "Invalid input or role"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden - requires admin role"),
            @ApiResponse(responseCode = "404", description = "User not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<User> changeUserRole(
            @Parameter(description = "User ID", required = true) @PathVariable Long userId,
            @RequestBody Map<String, String> roleMap) {
        log.info("Admin changing role for user ID: {}", userId);

        String roleName = roleMap.get("role");
        if (roleName == null) {
            return ResponseEntity.badRequest().build();
        }

        try {
            Role role = Role.valueOf(roleName);
            User updatedUser = adminService.changeUserRole(userId, role);
            return ResponseEntity.ok(updatedUser);
        } catch (IllegalArgumentException e) {
            log.warn("Invalid role specified: {}", roleName);
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Disables a user account.
     *
     * @param userId ID of the user to disable
     * @return ResponseEntity containing the updated user
     */
    @PutMapping("/users/{userId}/disable")
    @Operation(summary = "Disable user", description = "Disables a user account (admin only)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User disabled successfully",
                    content = @Content(schema = @Schema(implementation = User.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden - requires admin role or cannot disable admin account"),
            @ApiResponse(responseCode = "404", description = "User not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<User> disableUser(
            @Parameter(description = "User ID", required = true) @PathVariable Long userId) {
        log.info("Admin disabling user ID: {}", userId);
        User disabledUser = adminService.disableUser(userId);
        return ResponseEntity.ok(disabledUser);
    }

    /**
     * Enables a previously disabled user account.
     *
     * @param userId ID of the user to enable
     * @return ResponseEntity containing the updated user
     */
    @PutMapping("/users/{userId}/enable")
    @Operation(summary = "Enable user", description = "Enables a previously disabled user account (admin only)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User enabled successfully",
                    content = @Content(schema = @Schema(implementation = User.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden - requires admin role"),
            @ApiResponse(responseCode = "404", description = "User not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<User> enableUser(
            @Parameter(description = "User ID", required = true) @PathVariable Long userId) {
        log.info("Admin enabling user ID: {}", userId);
        User enabledUser = adminService.enableUser(userId);
        return ResponseEntity.ok(enabledUser);
    }
}