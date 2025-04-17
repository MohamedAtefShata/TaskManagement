package com.example.minitrello.service;

import com.example.minitrello.dto.project.ProjectCreateDto;
import com.example.minitrello.dto.project.ProjectDto;
import com.example.minitrello.dto.project.ProjectUpdateDto;
import com.example.minitrello.dto.user.UserDto;
import com.example.minitrello.exception.AccessDeniedException;
import com.example.minitrello.exception.ResourceNotFoundException;
import com.example.minitrello.mapper.ProjectMapper;
import com.example.minitrello.model.Project;
import com.example.minitrello.model.User;
import com.example.minitrello.repository.ProjectRepository;
import com.example.minitrello.repository.UserRepository;
import com.example.minitrello.service.interfaces.AuthService;
import com.example.minitrello.service.interfaces.ProjectService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * Implementation of the ProjectService interface.
 * Provides project management functionality.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ProjectServiceImpl implements ProjectService {

    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;
    private final ProjectMapper projectMapper;
    private final AuthService authService;

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public ProjectDto createProject(ProjectCreateDto createDto) {
        log.info("Creating new project: {}", createDto.getName());

        UserDto currentUserDto = authService.getCurrentAuthenticatedUserDto();
        User currentUser = userRepository.findById(currentUserDto.getId())
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", currentUserDto.getId()));

        Project project = projectMapper.toEntity(createDto, currentUser);
        Project savedProject = projectRepository.save(project);

        return projectMapper.toDto(savedProject);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public ProjectDto updateProject(Long projectId, ProjectUpdateDto updateDto) {
        log.info("Updating project with ID: {}", projectId);

        Long currentUserId = authService.getCurrentAuthenticatedUserId();

        // Check if user has access to the project
        if (!projectRepository.hasUserAccess(projectId, currentUserId)) {
            throw new AccessDeniedException("You don't have access to this project");
        }

        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project", "id", projectId));

        projectMapper.updateProjectFromDto(updateDto, project);
        Project updatedProject = projectRepository.save(project);

        return projectMapper.toDto(updatedProject);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(readOnly = true)
    public Optional<ProjectDto> findProjectById(Long projectId) {
        log.debug("Finding project by ID: {}", projectId);

        Long currentUserId = authService.getCurrentAuthenticatedUserId();

        return projectRepository.findByIdWithAccessCheck(projectId, currentUserId)
                .map(projectMapper::toDto);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(readOnly = true)
    public Page<ProjectDto> findAccessibleProjects(Pageable pageable) {
        log.debug("Finding all accessible projects with pagination");

        Long currentUserId = authService.getCurrentAuthenticatedUserId();

        return projectRepository.findAccessibleProjects(currentUserId, pageable)
                .map(projectMapper::toDto);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(readOnly = true)
    public Page<ProjectDto> findOwnedProjects(Pageable pageable) {
        log.debug("Finding owned projects with pagination");

        Long currentUserId = authService.getCurrentAuthenticatedUserId();
        User currentUser = userRepository.findById(currentUserId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", currentUserId));

        return projectRepository.findByOwner(currentUser, pageable)
                .map(projectMapper::toDto);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(readOnly = true)
    public Page<ProjectDto> findProjectsWhereUserIsMember(Pageable pageable) {
        log.debug("Finding projects where user is a member with pagination");

        Long currentUserId = authService.getCurrentAuthenticatedUserId();

        return projectRepository.findByMembersId(currentUserId, pageable)
                .map(projectMapper::toDto);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public boolean deleteProject(Long projectId) {
        log.info("Deleting project with ID: {}", projectId);

        Long currentUserId = authService.getCurrentAuthenticatedUserId();

        return projectRepository.findById(projectId)
                .map(project -> {
                    // Check if user is the owner
                    if (!project.getOwner().getId().equals(currentUserId)) {
                        throw new AccessDeniedException("Only the project owner can delete the project");
                    }

                    projectRepository.delete(project);
                    return true;
                })
                .orElse(false);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public ProjectDto addMemberToProject(Long projectId, Long userId) {
        log.info("Adding user ID: {} to project ID: {}", userId, projectId);

        Long currentUserId = authService.getCurrentAuthenticatedUserId();

        // Check if current user has access to the project
        if (!projectRepository.hasUserAccess(projectId, currentUserId)) {
            throw new AccessDeniedException("You don't have access to this project");
        }

        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project", "id", projectId));

        // Find the user to add
        User userToAdd = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        // Add the user to project members
        project.addMember(userToAdd);
        Project updatedProject = projectRepository.save(project);

        return projectMapper.toDto(updatedProject);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public ProjectDto removeMemberFromProject(Long projectId, Long userId) {
        log.info("Removing user ID: {} from project ID: {}", userId, projectId);

        Long currentUserId = authService.getCurrentAuthenticatedUserId();

        // Check if current user has access to the project
        if (!projectRepository.hasUserAccess(projectId, currentUserId)) {
            throw new AccessDeniedException("You don't have access to this project");
        }

        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project", "id", projectId));

        // Find the user to remove
        User userToRemove = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        // If the user is the owner, they can't be removed
        if (project.getOwner().getId().equals(userId)) {
            throw new IllegalStateException("Cannot remove the project owner");
        }

        // Remove the user from project members
        if (!project.removeMember(userToRemove)) {
            throw new IllegalStateException("User is not a member of this project");
        }

        Project updatedProject = projectRepository.save(project);

        return projectMapper.toDto(updatedProject);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public boolean leaveProject(Long projectId) {
        log.info("Current user leaving project ID: {}", projectId);

        Long currentUserId = authService.getCurrentAuthenticatedUserId();
        User currentUser = userRepository.findById(currentUserId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", currentUserId));

        return projectRepository.findById(projectId)
                .map(project -> {
                    // If the user is the owner, they can't leave
                    if (project.getOwner().getId().equals(currentUserId)) {
                        throw new IllegalStateException("Project owner cannot leave the project");
                    }

                    // Remove the user from project members
                    if (!project.removeMember(currentUser)) {
                        throw new IllegalStateException("User is not a member of this project");
                    }

                    projectRepository.save(project);
                    return true;
                })
                .orElseThrow(() -> new ResourceNotFoundException("Project", "id", projectId));
    }
}