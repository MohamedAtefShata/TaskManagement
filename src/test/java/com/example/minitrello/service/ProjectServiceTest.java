package com.example.minitrello.service;

import com.example.minitrello.dto.project.ProjectCreateDto;
import com.example.minitrello.dto.project.ProjectDto;
import com.example.minitrello.dto.project.ProjectUpdateDto;
import com.example.minitrello.dto.user.UserDto;
import com.example.minitrello.exception.AccessDeniedException;
import com.example.minitrello.exception.ResourceNotFoundException;
import com.example.minitrello.mapper.ProjectMapper;
import com.example.minitrello.model.Project;
import com.example.minitrello.model.Role;
import com.example.minitrello.model.User;
import com.example.minitrello.repository.ProjectRepository;
import com.example.minitrello.repository.UserRepository;
import com.example.minitrello.service.interfaces.AuthService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.Collections;
import java.util.HashSet;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProjectServiceTest {

    @Mock
    private ProjectRepository projectRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ProjectMapper projectMapper;

    @Mock
    private AuthService authService;

    @InjectMocks
    private ProjectServiceImpl projectService;

    private User testUser;
    private User testMember;
    private Project testProject;
    private ProjectDto testProjectDto;
    private ProjectCreateDto testCreateDto;
    private ProjectUpdateDto testUpdateDto;
    private UserDto testUserDto;
    private final Long USER_ID = 1L;
    private final Long MEMBER_ID = 2L;
    private final Long PROJECT_ID = 1L;

    @BeforeEach
    void setUp() {
        // Setup test user
        testUser = User.builder()
                .id(USER_ID)
                .name("Test User")
                .email("test@example.com")
                .role(Role.ROLE_USER)
                .projects(new HashSet<>())
                .build();

        // Setup test member
        testMember = User.builder()
                .id(MEMBER_ID)
                .name("Test Member")
                .email("member@example.com")
                .role(Role.ROLE_USER)
                .build();

        // Setup test project
        testProject = Project.builder()
                .id(PROJECT_ID)
                .name("Test Project")
                .description("Test Description")
                .owner(testUser)
                .members(new HashSet<>())
                .build();

        // Setup test project DTO
        testProjectDto = ProjectDto.builder()
                .id(PROJECT_ID)
                .name("Test Project")
                .description("Test Description")
                .ownerId(USER_ID)
                .ownerName("Test User")
                .memberCount(0)
                .taskListCount(0)
                .build();

        // Setup test create DTO
        testCreateDto = ProjectCreateDto.builder()
                .name("Test Project")
                .description("Test Description")
                .build();

        // Setup test update DTO
        testUpdateDto = ProjectUpdateDto.builder()
                .name("Updated Project")
                .description("Updated Description")
                .build();

        // Setup test user DTO
        testUserDto = UserDto.builder()
                .id(USER_ID)
                .name("Test User")
                .email("test@example.com")
                .role(Role.ROLE_USER)
                .build();
    }

    @Test
    void createProject_Success() {
        // Arrange
        when(authService.getCurrentAuthenticatedUserDto()).thenReturn(testUserDto);
        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(testUser));
        when(projectMapper.toEntity(testCreateDto, testUser)).thenReturn(testProject);
        when(projectRepository.save(any(Project.class))).thenReturn(testProject);
        when(projectMapper.toDto(testProject)).thenReturn(testProjectDto);

        // Act
        ProjectDto result = projectService.createProject(testCreateDto);

        // Assert
        assertNotNull(result);
        assertEquals(PROJECT_ID, result.getId());
        assertEquals("Test Project", result.getName());
        assertEquals("Test Description", result.getDescription());
        assertEquals(USER_ID, result.getOwnerId());

        // Verify
        verify(authService).getCurrentAuthenticatedUserDto();
        verify(userRepository).findById(USER_ID);
        verify(projectMapper).toEntity(testCreateDto, testUser);
        verify(projectRepository).save(testProject);
        verify(projectMapper).toDto(testProject);
    }

    @Test
    void createProject_UserNotFound_ThrowsException() {
        // Arrange
        when(authService.getCurrentAuthenticatedUserDto()).thenReturn(testUserDto);
        when(userRepository.findById(USER_ID)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () ->
                projectService.createProject(testCreateDto));

        // Verify
        verify(authService).getCurrentAuthenticatedUserDto();
        verify(userRepository).findById(USER_ID);
        verifyNoInteractions(projectMapper);
        verifyNoInteractions(projectRepository);
    }

    @Test
    void updateProject_Success() {
        // Arrange
        when(authService.getCurrentAuthenticatedUserId()).thenReturn(USER_ID);
        when(projectRepository.hasUserAccess(PROJECT_ID, USER_ID)).thenReturn(true);
        when(projectRepository.findById(PROJECT_ID)).thenReturn(Optional.of(testProject));
        when(projectRepository.save(testProject)).thenReturn(testProject);
        when(projectMapper.toDto(testProject)).thenReturn(testProjectDto);

        // Act
        ProjectDto result = projectService.updateProject(PROJECT_ID, testUpdateDto);

        // Assert
        assertNotNull(result);

        // Verify
        verify(authService).getCurrentAuthenticatedUserId();
        verify(projectRepository).hasUserAccess(PROJECT_ID, USER_ID);
        verify(projectRepository).findById(PROJECT_ID);
        verify(projectMapper).updateProjectFromDto(testUpdateDto, testProject);
        verify(projectRepository).save(testProject);
        verify(projectMapper).toDto(testProject);
    }

    @Test
    void updateProject_NoAccess_ThrowsException() {
        // Arrange
        when(authService.getCurrentAuthenticatedUserId()).thenReturn(USER_ID);
        when(projectRepository.hasUserAccess(PROJECT_ID, USER_ID)).thenReturn(false);

        // Act & Assert
        assertThrows(AccessDeniedException.class, () ->
                projectService.updateProject(PROJECT_ID, testUpdateDto));

        // Verify
        verify(authService).getCurrentAuthenticatedUserId();
        verify(projectRepository).hasUserAccess(PROJECT_ID, USER_ID);
        verifyNoMoreInteractions(projectRepository);
        verifyNoInteractions(projectMapper);
    }

    @Test
    void updateProject_ProjectNotFound_ThrowsException() {
        // Arrange
        when(authService.getCurrentAuthenticatedUserId()).thenReturn(USER_ID);
        when(projectRepository.hasUserAccess(PROJECT_ID, USER_ID)).thenReturn(true);
        when(projectRepository.findById(PROJECT_ID)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () ->
                projectService.updateProject(PROJECT_ID, testUpdateDto));

        // Verify
        verify(authService).getCurrentAuthenticatedUserId();
        verify(projectRepository).hasUserAccess(PROJECT_ID, USER_ID);
        verify(projectRepository).findById(PROJECT_ID);
        verifyNoInteractions(projectMapper);
    }

    @Test
    void findProjectById_Success() {
        // Arrange
        when(authService.getCurrentAuthenticatedUserId()).thenReturn(USER_ID);
        when(projectRepository.findByIdWithAccessCheck(PROJECT_ID, USER_ID))
                .thenReturn(Optional.of(testProject));
        when(projectMapper.toDto(testProject)).thenReturn(testProjectDto);

        // Act
        Optional<ProjectDto> result = projectService.findProjectById(PROJECT_ID);

        // Assert
        assertTrue(result.isPresent());
        assertEquals(PROJECT_ID, result.get().getId());

        // Verify
        verify(authService).getCurrentAuthenticatedUserId();
        verify(projectRepository).findByIdWithAccessCheck(PROJECT_ID, USER_ID);
        verify(projectMapper).toDto(testProject);
    }

    @Test
    void findProjectById_NoAccess_ReturnsEmpty() {
        // Arrange
        when(authService.getCurrentAuthenticatedUserId()).thenReturn(USER_ID);
        when(projectRepository.findByIdWithAccessCheck(PROJECT_ID, USER_ID))
                .thenReturn(Optional.empty());

        // Act
        Optional<ProjectDto> result = projectService.findProjectById(PROJECT_ID);

        // Assert
        assertFalse(result.isPresent());

        // Verify
        verify(authService).getCurrentAuthenticatedUserId();
        verify(projectRepository).findByIdWithAccessCheck(PROJECT_ID, USER_ID);
        verifyNoInteractions(projectMapper);
    }

    @Test
    void findAccessibleProjects_Success() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);
        Page<Project> projectPage = new PageImpl<>(Collections.singletonList(testProject));

        when(authService.getCurrentAuthenticatedUserId()).thenReturn(USER_ID);
        when(projectRepository.findAccessibleProjects(USER_ID, pageable)).thenReturn(projectPage);
        when(projectMapper.toDto(testProject)).thenReturn(testProjectDto);

        // Act
        Page<ProjectDto> result = projectService.findAccessibleProjects(pageable);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals(testProjectDto, result.getContent().get(0));

        // Verify
        verify(authService).getCurrentAuthenticatedUserId();
        verify(projectRepository).findAccessibleProjects(USER_ID, pageable);
        verify(projectMapper).toDto(testProject);
    }

    @Test
    void findOwnedProjects_Success() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);
        Page<Project> projectPage = new PageImpl<>(Collections.singletonList(testProject));

        when(authService.getCurrentAuthenticatedUserId()).thenReturn(USER_ID);
        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(testUser));
        when(projectRepository.findByOwner(testUser, pageable)).thenReturn(projectPage);
        when(projectMapper.toDto(testProject)).thenReturn(testProjectDto);

        // Act
        Page<ProjectDto> result = projectService.findOwnedProjects(pageable);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals(testProjectDto, result.getContent().get(0));

        // Verify
        verify(authService).getCurrentAuthenticatedUserId();
        verify(userRepository).findById(USER_ID);
        verify(projectRepository).findByOwner(testUser, pageable);
        verify(projectMapper).toDto(testProject);
    }

    @Test
    void findProjectsWhereUserIsMember_Success() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);
        Page<Project> projectPage = new PageImpl<>(Collections.singletonList(testProject));

        when(authService.getCurrentAuthenticatedUserId()).thenReturn(USER_ID);
        when(projectRepository.findByMembersId(USER_ID, pageable)).thenReturn(projectPage);
        when(projectMapper.toDto(testProject)).thenReturn(testProjectDto);

        // Act
        Page<ProjectDto> result = projectService.findProjectsWhereUserIsMember(pageable);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals(testProjectDto, result.getContent().get(0));

        // Verify
        verify(authService).getCurrentAuthenticatedUserId();
        verify(projectRepository).findByMembersId(USER_ID, pageable);
        verify(projectMapper).toDto(testProject);
    }

    @Test
    void deleteProject_Success() {
        // Arrange
        when(authService.getCurrentAuthenticatedUserId()).thenReturn(USER_ID);
        when(projectRepository.findById(PROJECT_ID)).thenReturn(Optional.of(testProject));

        // Act
        boolean result = projectService.deleteProject(PROJECT_ID);

        // Assert
        assertTrue(result);

        // Verify
        verify(authService).getCurrentAuthenticatedUserId();
        verify(projectRepository).findById(PROJECT_ID);
        verify(projectRepository).delete(testProject);
    }

    @Test
    void deleteProject_NotOwner_ThrowsException() {
        // Arrange
        testProject.setOwner(testMember); // Change owner to someone else

        when(authService.getCurrentAuthenticatedUserId()).thenReturn(USER_ID);
        when(projectRepository.findById(PROJECT_ID)).thenReturn(Optional.of(testProject));

        // Act & Assert
        assertThrows(AccessDeniedException.class, () ->
                projectService.deleteProject(PROJECT_ID));

        // Verify
        verify(authService).getCurrentAuthenticatedUserId();
        verify(projectRepository).findById(PROJECT_ID);
        verifyNoMoreInteractions(projectRepository);
    }

    @Test
    void deleteProject_ProjectNotFound_ReturnsFalse() {
        // Arrange
        when(authService.getCurrentAuthenticatedUserId()).thenReturn(USER_ID);
        when(projectRepository.findById(PROJECT_ID)).thenReturn(Optional.empty());

        // Act
        boolean result = projectService.deleteProject(PROJECT_ID);

        // Assert
        assertFalse(result);

        // Verify
        verify(authService).getCurrentAuthenticatedUserId();
        verify(projectRepository).findById(PROJECT_ID);
        verifyNoMoreInteractions(projectRepository);
    }

    @Test
    void addMemberToProject_Success() {
        // Arrange
        when(authService.getCurrentAuthenticatedUserId()).thenReturn(USER_ID);
        when(projectRepository.hasUserAccess(PROJECT_ID, USER_ID)).thenReturn(true);
        when(projectRepository.findById(PROJECT_ID)).thenReturn(Optional.of(testProject));
        when(userRepository.findById(MEMBER_ID)).thenReturn(Optional.of(testMember));
        when(projectRepository.save(testProject)).thenReturn(testProject);
        when(projectMapper.toDto(testProject)).thenReturn(testProjectDto);

        // Act
        ProjectDto result = projectService.addMemberToProject(PROJECT_ID, MEMBER_ID);

        // Assert
        assertNotNull(result);

        // Verify
        verify(authService).getCurrentAuthenticatedUserId();
        verify(projectRepository).hasUserAccess(PROJECT_ID, USER_ID);
        verify(projectRepository).findById(PROJECT_ID);
        verify(userRepository).findById(MEMBER_ID);
        verify(projectRepository).save(testProject);
        verify(projectMapper).toDto(testProject);
    }

    @Test
    void removeMemberFromProject_Success() {
        // Arrange
        testProject.getMembers().add(testMember);

        when(authService.getCurrentAuthenticatedUserId()).thenReturn(USER_ID);
        when(projectRepository.hasUserAccess(PROJECT_ID, USER_ID)).thenReturn(true);
        when(projectRepository.findById(PROJECT_ID)).thenReturn(Optional.of(testProject));
        when(userRepository.findById(MEMBER_ID)).thenReturn(Optional.of(testMember));
        when(projectRepository.save(testProject)).thenReturn(testProject);
        when(projectMapper.toDto(testProject)).thenReturn(testProjectDto);

        // Act
        ProjectDto result = projectService.removeMemberFromProject(PROJECT_ID, MEMBER_ID);

        // Assert
        assertNotNull(result);

        // Verify
        verify(authService).getCurrentAuthenticatedUserId();
        verify(projectRepository).hasUserAccess(PROJECT_ID, USER_ID);
        verify(projectRepository).findById(PROJECT_ID);
        verify(userRepository).findById(MEMBER_ID);
        verify(projectRepository).save(testProject);
        verify(projectMapper).toDto(testProject);
    }

    @Test
    void leaveProject_Success() {
        // Arrange
        testProject.setOwner(testMember); // Set someone else as owner
        testProject.getMembers().add(testUser); // Add current user as member

        when(authService.getCurrentAuthenticatedUserId()).thenReturn(USER_ID);
        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(testUser));
        when(projectRepository.findById(PROJECT_ID)).thenReturn(Optional.of(testProject));

        // Act
        boolean result = projectService.leaveProject(PROJECT_ID);

        // Assert
        assertTrue(result);

        // Verify
        verify(authService).getCurrentAuthenticatedUserId();
        verify(userRepository).findById(USER_ID);
        verify(projectRepository).findById(PROJECT_ID);
        verify(projectRepository).save(testProject);
    }

    @Test
    void leaveProject_UserIsOwner_ThrowsException() {
        // Arrange
        when(authService.getCurrentAuthenticatedUserId()).thenReturn(USER_ID);
        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(testUser));
        when(projectRepository.findById(PROJECT_ID)).thenReturn(Optional.of(testProject));

        // Act & Assert
        assertThrows(IllegalStateException.class, () ->
                projectService.leaveProject(PROJECT_ID));

        // Verify
        verify(authService).getCurrentAuthenticatedUserId();
        verify(userRepository).findById(USER_ID);
        verify(projectRepository).findById(PROJECT_ID);
    }
}