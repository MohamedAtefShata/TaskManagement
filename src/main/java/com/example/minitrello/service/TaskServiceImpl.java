package com.example.minitrello.service;

import com.example.minitrello.dto.task.TaskCreateDto;
import com.example.minitrello.dto.task.TaskDto;
import com.example.minitrello.dto.task.TaskMoveDto;
import com.example.minitrello.dto.task.TaskUpdateDto;
import com.example.minitrello.exception.AccessDeniedException;
import com.example.minitrello.exception.ResourceNotFoundException;
import com.example.minitrello.mapper.TaskMapper;
import com.example.minitrello.model.Task;
import com.example.minitrello.model.TaskList;
import com.example.minitrello.model.User;
import com.example.minitrello.repository.ProjectRepository;
import com.example.minitrello.repository.TaskListRepository;
import com.example.minitrello.repository.TaskRepository;
import com.example.minitrello.repository.UserRepository;
import com.example.minitrello.service.interfaces.AuthService;
import com.example.minitrello.service.interfaces.TaskService;
import com.example.minitrello.util.PositionUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Implementation of the TaskService interface.
 * Provides task management functionality.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class TaskServiceImpl implements TaskService {

    private final TaskRepository taskRepository;
    private final TaskListRepository taskListRepository;
    private final UserRepository userRepository;
    private final ProjectRepository projectRepository;
    private final TaskMapper taskMapper;
    private final AuthService authService;

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public TaskDto createTask(TaskCreateDto createDto) {
        log.info("Creating new task: {} for task list: {}", createDto.getTitle(), createDto.getTaskListId());

        Long currentUserId = authService.getCurrentAuthenticatedUserId();

        // Find task list
        TaskList taskList = taskListRepository.findById(createDto.getTaskListId())
                .orElseThrow(() -> new ResourceNotFoundException("TaskList", "id", createDto.getTaskListId()));

        // Check if user has access to the project
        if (!projectRepository.hasUserAccess(taskList.getProject().getId(), currentUserId)) {
            throw new AccessDeniedException("You don't have access to this project");
        }

        // Find assigned user if provided
        User assignedUser = null;
        if (createDto.getAssignedUserId() != null) {
            assignedUser = userRepository.findById(createDto.getAssignedUserId())
                    .orElseThrow(() -> new ResourceNotFoundException("User", "id", createDto.getAssignedUserId()));
        }

        // If position is not specified, put at the end
        if (createDto.getPosition() == null) {
            Integer maxPosition = taskRepository.findMaxPositionInTaskList(createDto.getTaskListId());
            createDto.setPosition(maxPosition != null ? maxPosition + 1 : 1);
        } else {
            // Shift other tasks to make room for the new position
            List<Task> tasksInList = taskRepository.findByTaskListId(createDto.getTaskListId());
            PositionUtils.shiftTasksForInsertion(tasksInList, createDto.getPosition());

            // Save all shifted tasks
            taskRepository.saveAll(tasksInList);
        }

        // Create task
        Task task = taskMapper.toEntity(createDto, taskList, assignedUser);
        Task savedTask = taskRepository.save(task);

        return taskMapper.toDto(savedTask);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public TaskDto updateTask(Long taskId, TaskUpdateDto updateDto) {
        log.info("Updating task with ID: {}", taskId);

        Long currentUserId = authService.getCurrentAuthenticatedUserId();

        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Task", "id", taskId));

        // Check if user has access to the project
        if (!projectRepository.hasUserAccess(task.getTaskList().getProject().getId(), currentUserId)) {
            throw new AccessDeniedException("You don't have access to this project");
        }

        // Update assigned user if provided
        if (updateDto.getAssignedUserId() != null) {
            User assignedUser = userRepository.findById(updateDto.getAssignedUserId())
                    .orElseThrow(() -> new ResourceNotFoundException("User", "id", updateDto.getAssignedUserId()));
            task.setAssignedUser(assignedUser);
        } else if (updateDto.getTitle() != null) {
            // If assignedUserId is explicitly set to null in the update, remove the assignment
            task.setAssignedUser(null);
        }

        // Handle position change within the same list
        if (updateDto.getPosition() != null && !updateDto.getPosition().equals(task.getPosition())) {
            List<Task> tasksInList = taskRepository.findByTaskListId(task.getTaskList().getId());

            // Remove current task from the list (to avoid duplicates in processing)
            tasksInList = tasksInList.stream()
                    .filter(t -> !t.getId().equals(taskId))
                    .collect(Collectors.toList());

            // Shift positions
            PositionUtils.shiftTasksForInsertion(tasksInList, updateDto.getPosition());

            // Save all shifted tasks
            taskRepository.saveAll(tasksInList);
        }

        // Update task fields
        taskMapper.updateTaskFromDto(updateDto, task);
        Task updatedTask = taskRepository.save(task);

        return taskMapper.toDto(updatedTask);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(readOnly = true)
    public Optional<TaskDto> findTaskById(Long taskId) {
        log.debug("Finding task by ID: {}", taskId);

        Long currentUserId = authService.getCurrentAuthenticatedUserId();

        return taskRepository.findById(taskId)
                .filter(task -> projectRepository.hasUserAccess(task.getTaskList().getProject().getId(), currentUserId))
                .map(taskMapper::toDto);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(readOnly = true)
    public List<TaskDto> findTasksByTaskList(Long taskListId) {
        log.debug("Finding tasks for task list with ID: {}", taskListId);

        Long currentUserId = authService.getCurrentAuthenticatedUserId();

        // Find task list
        TaskList taskList = taskListRepository.findById(taskListId)
                .orElseThrow(() -> new ResourceNotFoundException("TaskList", "id", taskListId));

        // Check if user has access to the project
        if (!projectRepository.hasUserAccess(taskList.getProject().getId(), currentUserId)) {
            throw new AccessDeniedException("You don't have access to this project");
        }

        // Return tasks ordered by position
        return taskList.getTasks().stream()
                .sorted((t1, t2) -> Integer.compare(t1.getPosition(), t2.getPosition()))
                .map(taskMapper::toDto)
                .collect(Collectors.toList());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public TaskDto moveTask(Long taskId, TaskMoveDto moveDto) {
        log.info("Moving task with ID: {} to task list: {}", taskId, moveDto.getTargetTaskListId());

        Long currentUserId = authService.getCurrentAuthenticatedUserId();

        // Find task
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Task", "id", taskId));

        // Check if user has access to the current project
        if (!projectRepository.hasUserAccess(task.getTaskList().getProject().getId(), currentUserId)) {
            throw new AccessDeniedException("You don't have access to this project");
        }

        // Find target task list
        TaskList targetTaskList = taskListRepository.findById(moveDto.getTargetTaskListId())
                .orElseThrow(() -> new ResourceNotFoundException("TaskList", "id", moveDto.getTargetTaskListId()));

        // Check if user has access to the target project
        if (!projectRepository.hasUserAccess(targetTaskList.getProject().getId(), currentUserId)) {
            throw new AccessDeniedException("You don't have access to the target project");
        }

        // Get current task list ID before moving
        Long oldTaskListId = task.getTaskList().getId();

        // If position is not specified, put at the end
        if (moveDto.getPosition() == null) {
            Integer maxPosition = taskRepository.findMaxPositionInTaskList(moveDto.getTargetTaskListId());
            moveDto.setPosition(maxPosition != null ? maxPosition + 1 : 1);
        } else {
            // Shift tasks in the target task list to make room
            List<Task> tasksInTargetList = taskRepository.findByTaskListId(moveDto.getTargetTaskListId());
            PositionUtils.shiftTasksForInsertion(tasksInTargetList, moveDto.getPosition());

            // Save all shifted tasks
            taskRepository.saveAll(tasksInTargetList);
        }

        // Move task
        task.setTaskList(targetTaskList);
        task.setPosition(moveDto.getPosition());
        Task movedTask = taskRepository.save(task);

        // Normalize positions in the old task list
        if (!oldTaskListId.equals(targetTaskList.getId())) {
            List<Task> tasksInOldList = taskRepository.findByTaskListId(oldTaskListId);
            List<Task> normalizedTasks = PositionUtils.normalizeTaskPositions(tasksInOldList);
            taskRepository.saveAll(normalizedTasks);
        }

        return taskMapper.toDto(movedTask);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public boolean deleteTask(Long taskId) {
        log.info("Deleting task with ID: {}", taskId);

        Long currentUserId = authService.getCurrentAuthenticatedUserId();

        return taskRepository.findById(taskId)
                .map(task -> {
                    // Check if user has access to the project
                    if (!projectRepository.hasUserAccess(task.getTaskList().getProject().getId(), currentUserId)) {
                        throw new AccessDeniedException("You don't have access to this project");
                    }

                    Long taskListId = task.getTaskList().getId();

                    // Delete the task
                    taskRepository.delete(task);

                    // Normalize positions for remaining tasks in the task list
                    List<Task> remainingTasks = taskRepository.findByTaskListId(taskListId);
                    if (!remainingTasks.isEmpty()) {
                        List<Task> normalizedTasks = PositionUtils.normalizeTaskPositions(remainingTasks);
                        taskRepository.saveAll(normalizedTasks);
                    }

                    return true;
                })
                .orElse(false);
    }
}