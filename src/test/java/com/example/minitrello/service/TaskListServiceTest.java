package com.example.minitrello.service;

import com.example.minitrello.dto.tasklist.TaskListCreateDto;
import com.example.minitrello.dto.tasklist.TaskListDto;
import com.example.minitrello.dto.tasklist.TaskListUpdateDto;
import com.example.minitrello.exception.ResourceNotFoundException;
import com.example.minitrello.mapper.TaskListMapper;
import com.example.minitrello.model.Project;
import com.example.minitrello.model.TaskList;
import com.example.minitrello.model.User;
import com.example.minitrello.repository.ProjectRepository;
import com.example.minitrello.repository.TaskListRepository;
import com.example.minitrello.service.interfaces.AuthService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class TaskListServiceTest {

    @Mock
    private TaskListRepository taskListRepository;

    @Mock
    private ProjectRepository projectRepository;

    @Mock
    private TaskListMapper taskListMapper;

    @Mock
    private AuthService authService;

    @InjectMocks
    private TaskListServiceImpl taskListService;

    private Project testProject;
    private TaskList testTaskList;
    private TaskListCreateDto testCreateDto;
    private TaskListUpdateDto testUpdateDto;
    private TaskListDto testTaskListDto;
    private final Long USER_ID = 1L;
    private final Long PROJECT_ID = 1L;
    private final Long TASKLIST_ID = 1L;

    @BeforeEach
    void setUp() {
        // Make the mocks lenient
        lenient().when(taskListRepository.findMaxPositionInProject(PROJECT_ID)).thenReturn(1);

        // Create test user
        User testUser = User.builder()
                .id(USER_ID)
                .name("Test User")
                .email("test@example.com")
                .build();

        // Create test project
        testProject = Project.builder()
                .id(PROJECT_ID)
                .name("Test Project")
                .owner(testUser)
                .members(new HashSet<>())
                .taskLists(new HashSet<>())
                .build();

        // Create test task list
        testTaskList = TaskList.builder()
                .id(TASKLIST_ID)
                .name("Test Task List")
                .position(1)
                .project(testProject)
                .tasks(new ArrayList<>())
                .build();

        // Create test DTOs
        testCreateDto = TaskListCreateDto.builder()
                .name("New Task List")
                .projectId(PROJECT_ID)
                .position(2)
                .build();

        testUpdateDto = TaskListUpdateDto.builder()
                .name("Updated Task List")
                .position(3)
                .build();

        testTaskListDto = TaskListDto.builder()
                .id(TASKLIST_ID)
                .name("Test Task List")
                .position(1)
                .projectId(PROJECT_ID)
                .projectName("Test Project")
                .taskCount(0)
                .tasks(new ArrayList<>())
                .build();
    }

    @Test
    void createTaskList_Success() {
        // Arrange
        when(authService.getCurrentAuthenticatedUserId()).thenReturn(USER_ID);
        when(projectRepository.findByIdWithAccessCheck(PROJECT_ID, USER_ID))
                .thenReturn(Optional.of(testProject));
        // Mock finding max position
        when(taskListRepository.findMaxPositionInProject(PROJECT_ID)).thenReturn(1);
        when(taskListMapper.toEntity(testCreateDto, testProject)).thenReturn(testTaskList);
        when(taskListRepository.save(any(TaskList.class))).thenReturn(testTaskList);
        when(taskListMapper.toDto(testTaskList)).thenReturn(testTaskListDto);

        // Act
        TaskListDto result = taskListService.createTaskList(testCreateDto);

        // Assert
        assertNotNull(result);
        assertEquals(TASKLIST_ID, result.getId());
        assertEquals(testTaskListDto.getName(), result.getName());
        assertEquals(testTaskListDto.getPosition(), result.getPosition());
        verify(taskListRepository).save(testTaskList);
    }

    @Test
    void createTaskList_NoProject_ThrowsException() {
        // Arrange
        when(authService.getCurrentAuthenticatedUserId()).thenReturn(USER_ID);
        when(projectRepository.findByIdWithAccessCheck(PROJECT_ID, USER_ID))
                .thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class,
                () -> taskListService.createTaskList(testCreateDto));
        verify(taskListRepository, never()).save(any());
    }

    @Test
    void updateTaskList_Success() {
        // Arrange
        when(authService.getCurrentAuthenticatedUserId()).thenReturn(USER_ID);
        when(taskListRepository.findById(TASKLIST_ID)).thenReturn(Optional.of(testTaskList));
        when(projectRepository.hasUserAccess(PROJECT_ID, USER_ID)).thenReturn(true);
        when(taskListRepository.save(any(TaskList.class))).thenReturn(testTaskList);
        when(taskListMapper.toDto(testTaskList)).thenReturn(testTaskListDto);

        // Act
        TaskListDto result = taskListService.updateTaskList(TASKLIST_ID, testUpdateDto);

        // Assert
        assertNotNull(result);
        verify(taskListMapper).updateTaskListFromDto(testUpdateDto, testTaskList);
        verify(taskListRepository).save(testTaskList);
    }

    @Test
    void deleteTaskList_Success() {
        // Arrange
        when(authService.getCurrentAuthenticatedUserId()).thenReturn(USER_ID);
        when(taskListRepository.findById(TASKLIST_ID)).thenReturn(Optional.of(testTaskList));
        when(projectRepository.hasUserAccess(PROJECT_ID, USER_ID)).thenReturn(true);

        // Act
        boolean result = taskListService.deleteTaskList(TASKLIST_ID);

        // Assert
        assertTrue(result);
        verify(taskListRepository).delete(testTaskList);
    }
}