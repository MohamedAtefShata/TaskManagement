package com.example.minitrello.service.interfaces;

import com.example.minitrello.dto.project.ProjectCreateDto;
import com.example.minitrello.dto.project.ProjectDto;
import com.example.minitrello.dto.project.ProjectUpdateDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;

import java.util.Optional;

/**
 * Service interface for managing project-related operations.
 */
@PreAuthorize("isAuthenticated()")
public interface ProjectService {

    /**
     * Creates a new project for the current user.
     *
     * @param createDto the DTO containing project information
     * @return the created project as DTO
     */
    ProjectDto createProject(ProjectCreateDto createDto);

    /**
     * Updates an existing project if the user has access.
     *
     * @param projectId the ID of the project to update
     * @param updateDto the DTO containing fields to update
     * @return the updated project as DTO
     */
    ProjectDto updateProject(Long projectId, ProjectUpdateDto updateDto);

    /**
     * Finds a project by ID if the user has access.
     *
     * @param projectId the ID of the project to find
     * @return an Optional containing the found project DTO, or empty if not found or no access
     */
    Optional<ProjectDto> findProjectById(Long projectId);

    /**
     * Retrieves all projects the current user has access to.
     *
     * @param pageable pagination information
     * @return a Page of project DTOs
     */
    Page<ProjectDto> findAccessibleProjects(Pageable pageable);

    /**
     * Retrieves all projects owned by the current user.
     *
     * @param pageable pagination information
     * @return a Page of project DTOs
     */
    Page<ProjectDto> findOwnedProjects(Pageable pageable);

    /**
     * Retrieves all projects where the current user is a member (but not owner).
     * This method specifically finds projects where the user has been added as a participant.
     *
     * @param pageable pagination information
     * @return a Page of project DTOs where the current user is a member
     */
    Page<ProjectDto> findProjectsWhereUserIsMember(Pageable pageable);

    /**
     * Deletes a project if the user is the owner.
     *
     * @param projectId the ID of the project to delete
     * @return true if the project was deleted, false otherwise
     */
    boolean deleteProject(Long projectId);

    /**
     * Adds a user to a project if the current user has access.
     *
     * @param projectId the ID of the project
     * @param userId the ID of the user to add
     * @return the updated project as DTO
     */
    ProjectDto addMemberToProject(Long projectId, Long userId);

    /**
     * Removes a user from a project if the current user has access.
     *
     * @param projectId the ID of the project
     * @param userId the ID of the user to remove
     * @return the updated project as DTO
     */
    ProjectDto removeMemberFromProject(Long projectId, Long userId);

    /**
     * Current user leaves a project they are a member of.
     *
     * @param projectId the ID of the project to leave
     * @return true if the user successfully left the project
     */
    boolean leaveProject(Long projectId);
}