package com.example.minitrello.service.interfaces;

import com.example.minitrello.dto.task.TaskCreateDto;
import com.example.minitrello.dto.task.TaskDto;
import com.example.minitrello.dto.task.TaskMoveDto;
import com.example.minitrello.dto.task.TaskUpdateDto;

import java.util.List;
import java.util.Optional;

/**
 * Service interface for managing task-related operations.
 */
public interface TaskService {

    /**
     * Creates a new task.
     *
     * @param createDto the DTO containing task information
     * @return the created task as DTO
     */
    TaskDto createTask(TaskCreateDto createDto);

    /**
     * Updates an existing task if the user has access.
     *
     * @param taskId the ID of the task to update
     * @param updateDto the DTO containing fields to update
     * @return the updated task as DTO
     */
    TaskDto updateTask(Long taskId, TaskUpdateDto updateDto);

    /**
     * Finds a task by ID if the user has access.
     *
     * @param taskId the ID of the task to find
     * @return an Optional containing the found task DTO, or empty if not found or no access
     */
    Optional<TaskDto> findTaskById(Long taskId);

    /**
     * Retrieves all tasks for a task list if the user has access.
     *
     * @param taskListId the ID of the task list
     * @return a List of task DTOs
     */
    List<TaskDto> findTasksByTaskList(Long taskListId);

    /**
     * Moves a task to a different task list if the user has access.
     *
     * @param taskId the ID of the task to move
     * @param moveDto the DTO containing target task list ID and position
     * @return the moved task as DTO
     */
    TaskDto moveTask(Long taskId, TaskMoveDto moveDto);

    /**
     * Deletes a task if the user has access.
     *
     * @param taskId the ID of the task to delete
     * @return true if the task was deleted, false otherwise
     */
    boolean deleteTask(Long taskId);
}