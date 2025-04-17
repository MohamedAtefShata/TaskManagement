// TaskServiceTest.java
package com.example.minitrello.service;

import com.example.minitrello.dto.task.TaskCreateDto;
import com.example.minitrello.dto.task.TaskDto;
import com.example.minitrello.dto.task.TaskMoveDto;
import com.example.minitrello.dto.task.TaskUpdateDto;
import com.example.minitrello.exception.ResourceNotFoundException;
import com.example.minitrello.mapper.TaskMapper;
import com.example.minitrello.model.Project;
import com.example.minitrello.model.Task;
import com.example.minitrello.model.TaskList;
import com.example.minitrello.model.User;
import com.example.minitrello.repository.ProjectRepository;
import com.example.minitrello.repository.TaskListRepository;
import com.example.minitrello.repository.TaskRepository;
import com.example.minitrello.repository.UserRepository;
import com.example.minitrello.service.interfaces.AuthService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class TaskServiceTest {

    @Mock
    private TaskRepository taskRepository;

    @Mock
    private TaskListRepository taskListRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ProjectRepository projectRepository;

    @Mock
    private TaskMapper taskMapper;

    @Mock
    private AuthService authService;

    @InjectMocks
    private TaskServiceImpl taskService;

    private User testUser;
    private TaskList testTaskList;
    private TaskList targetTaskList;
    private Task testTask;
    private TaskCreateDto testCreateDto;
    private TaskUpdateDto testUpdateDto;
    private TaskMoveDto testMoveDto;
    private TaskDto testTaskDto;

    private final Long USER_ID = 1L;
    private final Long PROJECT_ID = 1L;
    private final Long TASKLIST_ID = 1L;
    private final Long TARGET_TASKLIST_ID = 2L;
    private final Long TASK_ID = 1L;

    @BeforeEach
    void setUp() {
        // Create test user
        testUser = User.builder()
                .id(USER_ID)
                .name("Test User")
                .email("test@example.com")
                .build();

        // Create test project
        Project testProject = Project.builder()
                .id(PROJECT_ID)
                .name("Test Project")
                .owner(testUser)
                .build();

        // Create test task lists
        testTaskList = TaskList.builder()
                .id(TASKLIST_ID)
                .name("Test Task List")// Continuing TaskServiceTest.java
                .position(1)
                .project(testProject)
                .tasks(new ArrayList<>())
                .build();

        targetTaskList = TaskList.builder()
                .id(TARGET_TASKLIST_ID)
                .name("Target Task List")
                .position(2)
                .project(testProject)
                .tasks(new ArrayList<>())
                .build();

        // Create test task
        testTask = Task.builder()
                .id(TASK_ID)
                .title("Test Task")
                .description("Test Description")
                .position(1)
                .taskList(testTaskList)
                .assignedUser(testUser)
                .build();

        // Create test DTOs
        testCreateDto = TaskCreateDto.builder()
                .title("New Task")
                .description("New Description")
                .taskListId(TASKLIST_ID)
                .assignedUserId(USER_ID)
                .position(2)
                .build();

        testUpdateDto = TaskUpdateDto.builder()
                .title("Updated Task")
                .description("Updated Description")
                .assignedUserId(USER_ID)
                .position(3)
                .build();

        testMoveDto = TaskMoveDto.builder()
                .targetTaskListId(TARGET_TASKLIST_ID)
                .position(1)
                .build();

        testTaskDto = TaskDto.builder()
                .id(TASK_ID)
                .title("Test Task")
                .description("Test Description")
                .position(1)
                .taskListId(TASKLIST_ID)
                .taskListName("Test Task List")
                .assignedUserId(USER_ID)
                .assignedUserName("Test User")
                .build();
    }

    @Test
    void createTask_Success() {
        // Arrange
        when(authService.getCurrentAuthenticatedUserId()).thenReturn(USER_ID);
        when(taskListRepository.findById(TASKLIST_ID)).thenReturn(Optional.of(testTaskList));
        when(projectRepository.hasUserAccess(PROJECT_ID, USER_ID)).thenReturn(true);
        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(testUser));
        when(taskMapper.toEntity(testCreateDto, testTaskList, testUser)).thenReturn(testTask);
        when(taskRepository.save(any(Task.class))).thenReturn(testTask);
        when(taskMapper.toDto(testTask)).thenReturn(testTaskDto);

        // Act
        TaskDto result = taskService.createTask(testCreateDto);

        // Assert
        assertNotNull(result);
        assertEquals(TASK_ID, result.getId());
        verify(taskRepository).save(testTask);
    }

    @Test
    void createTask_NoTaskList_ThrowsException() {
        // Arrange
        when(authService.getCurrentAuthenticatedUserId()).thenReturn(USER_ID);
        when(taskListRepository.findById(TASKLIST_ID)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class,
                () -> taskService.createTask(testCreateDto));
        verify(taskRepository, never()).save(any());
    }

    @Test
    void updateTask_Success() {
        // Arrange
        when(authService.getCurrentAuthenticatedUserId()).thenReturn(USER_ID);
        when(taskRepository.findById(TASK_ID)).thenReturn(Optional.of(testTask));
        when(projectRepository.hasUserAccess(PROJECT_ID, USER_ID)).thenReturn(true);
        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(testUser));
        when(taskRepository.save(any(Task.class))).thenReturn(testTask);
        when(taskMapper.toDto(testTask)).thenReturn(testTaskDto);

        // Act
        TaskDto result = taskService.updateTask(TASK_ID, testUpdateDto);

        // Assert
        assertNotNull(result);
        verify(taskMapper).updateTaskFromDto(testUpdateDto, testTask);
        verify(taskRepository).save(testTask);
    }

    @Test
    void moveTask_Success() {
        // Arrange
        when(authService.getCurrentAuthenticatedUserId()).thenReturn(USER_ID);
        when(taskRepository.findById(TASK_ID)).thenReturn(Optional.of(testTask));
        when(projectRepository.hasUserAccess(PROJECT_ID, USER_ID)).thenReturn(true);
        when(taskListRepository.findById(TARGET_TASKLIST_ID)).thenReturn(Optional.of(targetTaskList));
        when(taskRepository.save(any(Task.class))).thenReturn(testTask);
        when(taskMapper.toDto(testTask)).thenReturn(testTaskDto);

        // Act
        TaskDto result = taskService.moveTask(TASK_ID, testMoveDto);

        // Assert
        assertNotNull(result);
        assertEquals(targetTaskList, testTask.getTaskList());
        assertEquals(testMoveDto.getPosition(), testTask.getPosition());
        verify(taskRepository).save(testTask);
    }

    @Test
    void deleteTask_Success() {
        // Arrange
        when(authService.getCurrentAuthenticatedUserId()).thenReturn(USER_ID);
        when(taskRepository.findById(TASK_ID)).thenReturn(Optional.of(testTask));
        when(projectRepository.hasUserAccess(PROJECT_ID, USER_ID)).thenReturn(true);

        // Act
        boolean result = taskService.deleteTask(TASK_ID);

        // Assert
        assertTrue(result);
        verify(taskRepository).delete(testTask);
    }
}