package com.example.minitrello.controller;

import com.example.minitrello.dto.user.UserDto;
import com.example.minitrello.dto.user.UserRoleUpdateDto;
import com.example.minitrello.service.interfaces.AdminService;
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
 * Controller for admin-only operations.
 * Provides APIs for user management tasks that require administrative privileges.
 * Only handles DTOs for request/response, never entities.
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
     * @return ResponseEntity containing a page of user DTOs with detailed information
     */
    @GetMapping("/users")
    @Operation(summary = "Get all users with details", description = "Retrieves detailed information about all users (admin only)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Users retrieved successfully"),
            @ApiResponse(responseCode = "401", ref = "Unauthorized"),
            @ApiResponse(responseCode = "403", ref = "Forbidden"),
            @ApiResponse(responseCode = "500", ref = "ServerError")
    })
    public ResponseEntity<Page<UserDto>> getAllUsersDetailed(Pageable pageable) {
        log.info("Admin fetching detailed user information with pagination");
        Page<UserDto> users = adminService.getAllUsersDetailed(pageable);
        return ResponseEntity.ok(users);
    }

    /**
     * Changes the role of a specific user.
     * Uses a dedicated DTO for role updates to improve API clarity.
     *
     * @param userId ID of the user whose role will be changed
     * @param roleUpdateDto DTO containing the new role
     * @return ResponseEntity containing the updated user DTO
     */
    @PutMapping("/users/{userId}/role")
    @Operation(summary = "Change user role", description = "Changes the role of a specific user (admin only)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Role changed successfully",
                    content = @Content(schema = @Schema(implementation = UserDto.class))),
            @ApiResponse(responseCode = "400", ref = "BadRequest"),
            @ApiResponse(responseCode = "401", ref = "Unauthorized"),
            @ApiResponse(responseCode = "403", ref = "Forbidden"),
            @ApiResponse(responseCode = "404", ref = "NotFound"),
            @ApiResponse(responseCode = "500", ref = "ServerError")
    })
    public ResponseEntity<UserDto> changeUserRole(
            @Parameter(description = "User ID", required = true) @PathVariable Long userId,
            @Valid @RequestBody UserRoleUpdateDto roleUpdateDto) {
        log.info("Admin changing role for user ID: {} to {}", userId, roleUpdateDto.getRole());
        UserDto updatedUser = adminService.changeUserRole(userId, roleUpdateDto.getRole());
        return ResponseEntity.ok(updatedUser);
    }

    /**
     * Disables a user account.
     *
     * @param userId ID of the user to disable
     * @return ResponseEntity containing the updated user DTO
     */
    @PutMapping("/users/{userId}/disable")
    @Operation(summary = "Disable user", description = "Disables a user account (admin only)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User disabled successfully",
                    content = @Content(schema = @Schema(implementation = UserDto.class))),
            @ApiResponse(responseCode = "401", ref = "Unauthorized"),
            @ApiResponse(responseCode = "403", ref = "Forbidden"),
            @ApiResponse(responseCode = "404", ref = "NotFound"),
            @ApiResponse(responseCode = "500", ref = "ServerError")
    })
    public ResponseEntity<UserDto> disableUser(
            @Parameter(description = "User ID", required = true) @PathVariable Long userId) {
        log.info("Admin disabling user ID: {}", userId);
        UserDto disabledUser = adminService.disableUser(userId);
        return ResponseEntity.ok(disabledUser);
    }

    /**
     * Enables a previously disabled user account.
     *
     * @param userId ID of the user to enable
     * @return ResponseEntity containing the updated user DTO
     */
    @PutMapping("/users/{userId}/enable")
    @Operation(summary = "Enable user", description = "Enables a previously disabled user account (admin only)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User enabled successfully",
                    content = @Content(schema = @Schema(implementation = UserDto.class))),
            @ApiResponse(responseCode = "401", ref = "Unauthorized"),
            @ApiResponse(responseCode = "403", ref = "Forbidden"),
            @ApiResponse(responseCode = "404", ref = "NotFound"),
            @ApiResponse(responseCode = "500", ref = "ServerError")
    })
    public ResponseEntity<UserDto> enableUser(
            @Parameter(description = "User ID", required = true) @PathVariable Long userId) {
        log.info("Admin enabling user ID: {}", userId);
        UserDto enabledUser = adminService.enableUser(userId);
        return ResponseEntity.ok(enabledUser);
    }
}