package com.example.minitrello.controller;

import com.example.minitrello.dto.project.ProjectCreateDto;
import com.example.minitrello.dto.project.ProjectDto;
import com.example.minitrello.dto.project.ProjectUpdateDto;
import com.example.minitrello.service.interfaces.ProjectService;
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
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Controller for managing project-related operations.
 * Provides APIs for creating, fetching, updating, and deleting projects.
 */
@RestController
@RequestMapping("/api/projects")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Projects", description = "Project management APIs")
@SecurityRequirement(name = "bearerAuth")
public class ProjectController {

    private final ProjectService projectService;

    /**
     * Creates a new project.
     *
     * @param createDto the DTO containing project information
     * @return ResponseEntity containing the created project DTO
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create new project", description = "Creates a new project for the current user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Project created successfully",
                    content = @Content(schema = @Schema(implementation = ProjectDto.class))),
            @ApiResponse(responseCode = "400", description = "Invalid input"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<ProjectDto> createProject(@Valid @RequestBody ProjectCreateDto createDto) {
        log.info("Creating new project: {}", createDto.getName());
        ProjectDto createdProject = projectService.createProject(createDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdProject);
    }

    /**
     * Retrieves a specific project by ID if the user has access.
     *
     * @param projectId ID of the project to retrieve
     * @return ResponseEntity containing the project DTO
     */
    @GetMapping("/{projectId}")
    @Operation(summary = "Get project by ID", description = "Retrieves a specific project by ID if the user has access")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Project retrieved successfully",
                    content = @Content(schema = @Schema(implementation = ProjectDto.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "404", description = "Project not found or no access"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<ProjectDto> getProjectById(
            @Parameter(description = "Project ID", required = true) @PathVariable Long projectId) {
        log.debug("Fetching project with ID: {}", projectId);
        return projectService.findProjectById(projectId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Retrieves all projects the current user has access to.
     *
     * @param pageable pagination information
     * @return ResponseEntity containing a page of project DTOs
     */
    @GetMapping
    @Operation(summary = "Get accessible projects", description = "Retrieves all projects the current user has access to")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Projects retrieved successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<Page<ProjectDto>> getAccessibleProjects(Pageable pageable) {
        log.debug("Fetching all accessible projects with pagination");
        Page<ProjectDto> projects = projectService.findAccessibleProjects(pageable);
        return ResponseEntity.ok(projects);
    }

    /**
     * Retrieves all projects owned by the current user.
     *
     * @param pageable pagination information
     * @return ResponseEntity containing a page of project DTOs
     */
    @GetMapping("/owned")
    @Operation(summary = "Get owned projects", description = "Retrieves all projects owned by the current user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Projects retrieved successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<Page<ProjectDto>> getOwnedProjects(Pageable pageable) {
        log.debug("Fetching owned projects with pagination");
        Page<ProjectDto> projects = projectService.findOwnedProjects(pageable);
        return ResponseEntity.ok(projects);
    }

    /**
     * Updates an existing project if the user has access.
     *
     * @param projectId ID of the project to update
     * @param updateDto DTO containing project information to update
     * @return ResponseEntity containing the updated project DTO
     */
    @PutMapping("/{projectId}")
    @Operation(summary = "Update project", description = "Updates an existing project if the user has access")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Project updated successfully",
                    content = @Content(schema = @Schema(implementation = ProjectDto.class))),
            @ApiResponse(responseCode = "400", description = "Invalid input"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "404", description = "Project not found or no access"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<ProjectDto> updateProject(
            @Parameter(description = "Project ID", required = true) @PathVariable Long projectId,
            @Valid @RequestBody ProjectUpdateDto updateDto) {
        log.info("Updating project with ID: {}", projectId);
        ProjectDto updatedProject = projectService.updateProject(projectId, updateDto);
        return ResponseEntity.ok(updatedProject);
    }

    /**
     * Deletes a project if the user is the owner.
     *
     * @param projectId ID of the project to delete
     * @return ResponseEntity with no content if successful
     */
    @DeleteMapping("/{projectId}")
    @Operation(summary = "Delete project", description = "Deletes a project if the user is the owner")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Project deleted successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden - user is not the project owner"),
            @ApiResponse(responseCode = "404", description = "Project not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<Void> deleteProject(
            @Parameter(description = "Project ID", required = true) @PathVariable Long projectId) {
        log.info("Deleting project with ID: {}", projectId);
        boolean deleted = projectService.deleteProject(projectId);
        return deleted ? ResponseEntity.noContent().build() : ResponseEntity.notFound().build();
    }

    /**
     * Adds a user to a project if the current user has access.
     *
     * @param projectId ID of the project
     * @param userId ID of the user to add
     * @return ResponseEntity containing the updated project DTO
     */
    @PostMapping("/{projectId}/members/{userId}")
    @Operation(summary = "Add member to project", description = "Adds a user to a project if the current user has access")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User added to project successfully",
                    content = @Content(schema = @Schema(implementation = ProjectDto.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "404", description = "Project or user not found or no access"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<ProjectDto> addMemberToProject(
            @Parameter(description = "Project ID", required = true) @PathVariable Long projectId,
            @Parameter(description = "User ID", required = true) @PathVariable Long userId) {
        log.info("Adding user ID: {} to project ID: {}", userId, projectId);
        ProjectDto updatedProject = projectService.addMemberToProject(projectId, userId);
        return ResponseEntity.ok(updatedProject);
    }

    /**
     * Removes a user from a project if the current user has access.
     *
     * @param projectId ID of the project
     * @param userId ID of the user to remove
     * @return ResponseEntity containing the updated project DTO
     */
    @DeleteMapping("/{projectId}/members/{userId}")
    @Operation(summary = "Remove member from project", description = "Removes a user from a project if the current user has access")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User removed from project successfully",
                    content = @Content(schema = @Schema(implementation = ProjectDto.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "404", description = "Project or user not found or no access"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<ProjectDto> removeMemberFromProject(
            @Parameter(description = "Project ID", required = true) @PathVariable Long projectId,
            @Parameter(description = "User ID", required = true) @PathVariable Long userId) {
        log.info("Removing user ID: {} from project ID: {}", userId, projectId);
        ProjectDto updatedProject = projectService.removeMemberFromProject(projectId, userId);
        return ResponseEntity.ok(updatedProject);
    }

    /**
     * Current user leaves a project they are a member of.
     *
     * @param projectId ID of the project to leave
     * @return ResponseEntity with no content if successful
     */
    @PostMapping("/{projectId}/leave")
    @Operation(summary = "Leave project", description = "Current user leaves a project they are a member of")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Left project successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden - user is the project owner"),
            @ApiResponse(responseCode = "404", description = "Project not found or user not a member"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<Void> leaveProject(
            @Parameter(description = "Project ID", required = true) @PathVariable Long projectId) {
        log.info("Current user leaving project ID: {}", projectId);
        boolean left = projectService.leaveProject(projectId);
        return left ? ResponseEntity.noContent().build() : ResponseEntity.notFound().build();
    }

    /**
     * Retrieves all projects where the current user is a member (but not owner).
     *
     * @param pageable pagination information
     * @return ResponseEntity containing a page of project DTOs
     */
    @GetMapping("/memberships")
    @Operation(summary = "Get projects as member", description = "Retrieves all projects where the current user is a member but not owner")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Projects retrieved successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<Page<ProjectDto>> getProjectsWhereUserIsMember(Pageable pageable) {
        log.debug("Fetching projects where user is a member with pagination");
        Page<ProjectDto> projects = projectService.findProjectsWhereUserIsMember(pageable);
        return ResponseEntity.ok(projects);
    }
}