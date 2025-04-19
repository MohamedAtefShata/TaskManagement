package com.example.minitrello.controller;

import com.example.minitrello.dto.task.TaskCreateDto;
import com.example.minitrello.dto.task.TaskDto;
import com.example.minitrello.dto.task.TaskMoveDto;
import com.example.minitrello.dto.task.TaskUpdateDto;
import com.example.minitrello.service.interfaces.TaskService;
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
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controller for managing task-related operations.
 * Provides APIs for creating, fetching, updating, moving, and deleting tasks.
 */
@RestController
@RequestMapping("/api/tasks")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Tasks", description = "Task management APIs")
@SecurityRequirement(name = "bearerAuth")
public class TaskController {

    private final TaskService taskService;

    /**
     * Creates a new task.
     *
     * @param createDto the DTO containing task information
     * @return ResponseEntity containing the created task DTO
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create new task", description = "Creates a new task within a task list")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Task created successfully",
                    content = @Content(schema = @Schema(implementation = TaskDto.class))),
            @ApiResponse(responseCode = "400", ref = "BadRequest"),
            @ApiResponse(responseCode = "401", ref = "Unauthorized"),
            @ApiResponse(responseCode = "404", ref = "NotFound"),
            @ApiResponse(responseCode = "500", ref = "ServerError")
    })
    public ResponseEntity<TaskDto> createTask(@Valid @RequestBody TaskCreateDto createDto) {
        log.info("Creating new task: {} for task list: {}", createDto.getTitle(), createDto.getTaskListId());
        TaskDto createdTask = taskService.createTask(createDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdTask);
    }

    /**
     * Retrieves a specific task by ID if the user has access.
     *
     * @param taskId ID of the task to retrieve
     * @return ResponseEntity containing the task DTO
     */
    @GetMapping("/{taskId}")
    @Operation(summary = "Get task by ID", description = "Retrieves a specific task by ID if the user has access")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Task retrieved successfully",
                    content = @Content(schema = @Schema(implementation = TaskDto.class))),
            @ApiResponse(responseCode = "401", ref = "Unauthorized"),
            @ApiResponse(responseCode = "404", ref = "NotFound"),
            @ApiResponse(responseCode = "500", ref = "ServerError")
    })
    public ResponseEntity<TaskDto> getTaskById(
            @Parameter(description = "Task ID", required = true) @PathVariable Long taskId) {
        log.debug("Fetching task with ID: {}", taskId);
        return taskService.findTaskById(taskId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Retrieves all tasks within a task list if the user has access.
     *
     * @param taskListId ID of the task list to retrieve tasks for
     * @return ResponseEntity containing a list of task DTOs
     */
    @GetMapping("/list/{taskListId}")
    @Operation(summary = "Get tasks by task list", description = "Retrieves all tasks within a task list if the user has access")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Tasks retrieved successfully"),
            @ApiResponse(responseCode = "401", ref = "Unauthorized"),
            @ApiResponse(responseCode = "404", ref = "NotFound"),
            @ApiResponse(responseCode = "500", ref = "ServerError")
    })
    public ResponseEntity<List<TaskDto>> getTasksByTaskList(
            @Parameter(description = "Task list ID", required = true) @PathVariable Long taskListId) {
        log.debug("Fetching tasks for task list with ID: {}", taskListId);
        List<TaskDto> tasks = taskService.findTasksByTaskList(taskListId);
        return ResponseEntity.ok(tasks);
    }

    /**
     * Updates an existing task if the user has access.
     *
     * @param taskId ID of the task to update
     * @param updateDto DTO containing task information to update
     * @return ResponseEntity containing the updated task DTO
     */
    @PutMapping("/{taskId}")
    @Operation(summary = "Update task", description = "Updates an existing task if the user has access")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Task updated successfully",
                    content = @Content(schema = @Schema(implementation = TaskDto.class))),
            @ApiResponse(responseCode = "400", ref = "BadRequest"),
            @ApiResponse(responseCode = "401", ref = "Unauthorized"),
            @ApiResponse(responseCode = "404", ref = "NotFound"),
            @ApiResponse(responseCode = "500", ref = "ServerError")
    })
    public ResponseEntity<TaskDto> updateTask(
            @Parameter(description = "Task ID", required = true) @PathVariable Long taskId,
            @Valid @RequestBody TaskUpdateDto updateDto) {
        log.info("Updating task with ID: {}", taskId);
        TaskDto updatedTask = taskService.updateTask(taskId, updateDto);
        return ResponseEntity.ok(updatedTask);
    }

    /**
     * Moves a task to a different task list if the user has access.
     *
     * @param taskId ID of the task to move
     * @param moveDto DTO containing target task list ID and new position
     * @return ResponseEntity containing the moved task DTO
     */
    @PutMapping("/{taskId}/move")
    @Operation(summary = "Move task", description = "Moves a task to a different task list if the user has access")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Task moved successfully",
                    content = @Content(schema = @Schema(implementation = TaskDto.class))),
            @ApiResponse(responseCode = "400", ref = "BadRequest"),
            @ApiResponse(responseCode = "401", ref = "Unauthorized"),
            @ApiResponse(responseCode = "404", ref = "NotFound"),
            @ApiResponse(responseCode = "500", ref = "ServerError")
    })
    public ResponseEntity<TaskDto> moveTask(
            @Parameter(description = "Task ID", required = true) @PathVariable Long taskId,
            @Valid @RequestBody TaskMoveDto moveDto) {
        log.info("Moving task with ID: {} to task list: {}", taskId, moveDto.getTargetTaskListId());
        TaskDto movedTask = taskService.moveTask(taskId, moveDto);
        return ResponseEntity.ok(movedTask);
    }

    /**
     * Deletes a task if the user has access.
     *
     * @param taskId ID of the task to delete
     * @return ResponseEntity with no content if successful
     */
    @DeleteMapping("/{taskId}")
    @Operation(summary = "Delete task", description = "Deletes a task if the user has access")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Task deleted successfully"),
            @ApiResponse(responseCode = "401", ref = "Unauthorized"),
            @ApiResponse(responseCode = "404", ref = "NotFound"),
            @ApiResponse(responseCode = "500", ref = "ServerError")
    })
    public ResponseEntity<Void> deleteTask(
            @Parameter(description = "Task ID", required = true) @PathVariable Long taskId) {
        log.info("Deleting task with ID: {}", taskId);
        boolean deleted = taskService.deleteTask(taskId);
        return deleted ? ResponseEntity.noContent().build() : ResponseEntity.notFound().build();
    }
}