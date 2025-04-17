package com.example.minitrello.service;

import com.example.minitrello.dto.tasklist.TaskListCreateDto;
import com.example.minitrello.dto.tasklist.TaskListDto;
import com.example.minitrello.dto.tasklist.TaskListUpdateDto;
import com.example.minitrello.exception.AccessDeniedException;
import com.example.minitrello.exception.ResourceNotFoundException;
import com.example.minitrello.mapper.TaskListMapper;
import com.example.minitrello.model.Project;
import com.example.minitrello.model.TaskList;
import com.example.minitrello.repository.ProjectRepository;
import com.example.minitrello.repository.TaskListRepository;
import com.example.minitrello.service.interfaces.AuthService;
import com.example.minitrello.service.interfaces.TaskListService;
import com.example.minitrello.util.PositionUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Implementation of the TaskListService interface.
 * Provides task list management functionality.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class TaskListServiceImpl implements TaskListService {

    private final TaskListRepository taskListRepository;
    private final ProjectRepository projectRepository;
    private final TaskListMapper taskListMapper;
    private final AuthService authService;

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public TaskListDto createTaskList(TaskListCreateDto createDto) {
        log.info("Creating new task list: {} for project: {}", createDto.getName(), createDto.getProjectId());

        Long currentUserId = authService.getCurrentAuthenticatedUserId();

        // Check if user has access to the project
        Project project = projectRepository.findByIdWithAccessCheck(createDto.getProjectId(), currentUserId)
                .orElseThrow(() -> new ResourceNotFoundException("Project", "id", createDto.getProjectId()));

        // If position is not specified, put at the end
        if (createDto.getPosition() == null) {
            Integer maxPosition = taskListRepository.findMaxPositionInProject(createDto.getProjectId());
            createDto.setPosition(maxPosition != null ? maxPosition + 1 : 1);
        } else {
            // Shift other task lists to make room for the new position
            List<TaskList> projectTaskLists = new ArrayList<>(project.getTaskLists());
            PositionUtils.shiftTaskListsForInsertion(projectTaskLists, createDto.getPosition());

            // Save all shifted task lists
            taskListRepository.saveAll(projectTaskLists);
        }

        // Create task list
        TaskList taskList = taskListMapper.toEntity(createDto, project);
        TaskList savedTaskList = taskListRepository.save(taskList);

        return taskListMapper.toDto(savedTaskList);
    }


    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public TaskListDto updateTaskList(Long taskListId, TaskListUpdateDto updateDto) {
        log.info("Updating task list with ID: {}", taskListId);

        Long currentUserId = authService.getCurrentAuthenticatedUserId();

        TaskList taskList = taskListRepository.findById(taskListId)
                .orElseThrow(() -> new ResourceNotFoundException("TaskList", "id", taskListId));

        // Check if user has access to the project
        if (!projectRepository.hasUserAccess(taskList.getProject().getId(), currentUserId)) {
            throw new AccessDeniedException("You don't have access to this project");
        }

        // Handle position change
        if (updateDto.getPosition() != null && !updateDto.getPosition().equals(taskList.getPosition())) {
            List<TaskList> projectTaskLists = taskListRepository.findByProjectId(taskList.getProject().getId());

            // Remove current task list from the list (to avoid duplicates in processing)
            projectTaskLists = projectTaskLists.stream()
                    .filter(tl -> !tl.getId().equals(taskListId))
                    .collect(Collectors.toList());

            // Shift positions
            PositionUtils.shiftTaskListsForInsertion(projectTaskLists, updateDto.getPosition());

            // Save all shifted task lists
            taskListRepository.saveAll(projectTaskLists);
        }

        // Update task list
        taskListMapper.updateTaskListFromDto(updateDto, taskList);
        TaskList updatedTaskList = taskListRepository.save(taskList);

        return taskListMapper.toDto(updatedTaskList);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(readOnly = true)
    public Optional<TaskListDto> findTaskListById(Long taskListId) {
        log.debug("Finding task list by ID: {}", taskListId);

        Long currentUserId = authService.getCurrentAuthenticatedUserId();

        return taskListRepository.findById(taskListId)
                .filter(taskList -> projectRepository.hasUserAccess(taskList.getProject().getId(), currentUserId))
                .map(taskListMapper::toDto);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(readOnly = true)
    public List<TaskListDto> findTaskListsByProject(Long projectId) {
        log.debug("Finding task lists for project with ID: {}", projectId);

        Long currentUserId = authService.getCurrentAuthenticatedUserId();

        // Check if user has access to the project
        Project project = projectRepository.findByIdWithAccessCheck(projectId, currentUserId)
                .orElseThrow(() -> new ResourceNotFoundException("Project", "id", projectId));

        // Return task lists ordered by position
        return project.getTaskLists().stream()
                .sorted(Comparator.comparingInt(TaskList::getPosition))
                .map(taskListMapper::toDto)
                .collect(Collectors.toList());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public boolean deleteTaskList(Long taskListId) {
        log.info("Deleting task list with ID: {}", taskListId);

        Long currentUserId = authService.getCurrentAuthenticatedUserId();

        return taskListRepository.findById(taskListId)
                .map(taskList -> {
                    // Check if user has access to the project
                    if (!projectRepository.hasUserAccess(taskList.getProject().getId(), currentUserId)) {
                        throw new AccessDeniedException("You don't have access to this project");
                    }

                    // Get all task lists in the project to normalize positions after deletion
                    List<TaskList> remainingTaskLists = taskListRepository.findByProjectId(taskList.getProject().getId());
                    remainingTaskLists.remove(taskList);

                    // Delete the task list
                    taskListRepository.delete(taskList);

                    // Normalize positions for remaining task lists
                    if (!remainingTaskLists.isEmpty()) {
                        List<TaskList> normalizedTaskLists =
                                PositionUtils.normalizeTaskListPositions(remainingTaskLists);
                        taskListRepository.saveAll(normalizedTaskLists);
                    }

                    return true;
                })
                .orElse(false);
    }
}