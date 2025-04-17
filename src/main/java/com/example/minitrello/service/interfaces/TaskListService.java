package com.example.minitrello.service.interfaces;

import com.example.minitrello.dto.tasklist.TaskListCreateDto;
import com.example.minitrello.dto.tasklist.TaskListDto;
import com.example.minitrello.dto.tasklist.TaskListUpdateDto;

import java.util.List;
import java.util.Optional;

/**
 * Service interface for managing task list-related operations.
 */
public interface TaskListService {

    /**
     * Creates a new task list.
     *
     * @param createDto the DTO containing task list information
     * @return the created task list as DTO
     */
    TaskListDto createTaskList(TaskListCreateDto createDto);

    /**
     * Updates an existing task list if the user has access.
     *
     * @param taskListId the ID of the task list to update
     * @param updateDto the DTO containing fields to update
     * @return the updated task list as DTO
     */
    TaskListDto updateTaskList(Long taskListId, TaskListUpdateDto updateDto);

    /**
     * Finds a task list by ID if the user has access.
     *
     * @param taskListId the ID of the task list to find
     * @return an Optional containing the found task list DTO, or empty if not found or no access
     */
    Optional<TaskListDto> findTaskListById(Long taskListId);

    /**
     * Retrieves all task lists for a project if the user has access.
     *
     * @param projectId the ID of the project
     * @return a List of task list DTOs
     */
    List<TaskListDto> findTaskListsByProject(Long projectId);

    /**
     * Deletes a task list if the user has access.
     *
     * @param taskListId the ID of the task list to delete
     * @return true if the task list was deleted, false otherwise
     */
    boolean deleteTaskList(Long taskListId);
}