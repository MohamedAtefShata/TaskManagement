package com.example.minitrello.controller;

import com.example.minitrello.dto.tasklist.TaskListCreateDto;
import com.example.minitrello.dto.tasklist.TaskListDto;
import com.example.minitrello.dto.tasklist.TaskListUpdateDto;
import com.example.minitrello.service.interfaces.TaskListService;
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
 * Controller for managing tasklist-related operations.
 * Provides APIs for creating, fetching, updating, and deleting task lists.
 */
@RestController
@RequestMapping("/api/tasklists")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Task Lists", description = "Task list management APIs")
@SecurityRequirement(name = "bearerAuth")
public class TaskListController {

    private final TaskListService taskListService;

    /**
     * Creates a new task list.
     *
     * @param createDto the DTO containing task list information
     * @return ResponseEntity containing the created task list DTO
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create new task list", description = "Creates a new task list within a project")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Task list created successfully",
                    content = @Content(schema = @Schema(implementation = TaskListDto.class))),
            @ApiResponse(responseCode = "400", ref = "BadRequest"),
            @ApiResponse(responseCode = "401", ref = "Unauthorized"),
            @ApiResponse(responseCode = "404", ref = "NotFound"),
            @ApiResponse(responseCode = "500", ref = "ServerError")
    })
    public ResponseEntity<TaskListDto> createTaskList(@Valid @RequestBody TaskListCreateDto createDto) {
        log.info("Creating new task list: {} for project: {}", createDto.getName(), createDto.getProjectId());
        TaskListDto createdTaskList = taskListService.createTaskList(createDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdTaskList);
    }

    /**
     * Retrieves a specific task list by ID if the user has access.
     *
     * @param taskListId ID of the task list to retrieve
     * @return ResponseEntity containing the task list DTO
     */
    @GetMapping("/{taskListId}")
    @Operation(summary = "Get task list by ID", description = "Retrieves a specific task list by ID if the user has access")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Task list retrieved successfully",
                    content = @Content(schema = @Schema(implementation = TaskListDto.class))),
            @ApiResponse(responseCode = "401", ref = "Unauthorized"),
            @ApiResponse(responseCode = "404", ref = "NotFound"),
            @ApiResponse(responseCode = "500", ref = "ServerError")
    })
    public ResponseEntity<TaskListDto> getTaskListById(
            @Parameter(description = "Task list ID", required = true) @PathVariable Long taskListId) {
        log.debug("Fetching task list with ID: {}", taskListId);
        return taskListService.findTaskListById(taskListId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Retrieves all task lists within a project if the user has access.
     *
     * @param projectId ID of the project to retrieve task lists for
     * @return ResponseEntity containing a list of task list DTOs
     */
    @GetMapping("/project/{projectId}")
    @Operation(summary = "Get task lists by project", description = "Retrieves all task lists within a project if the user has access")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Task lists retrieved successfully"),
            @ApiResponse(responseCode = "401", ref = "Unauthorized"),
            @ApiResponse(responseCode = "404", ref = "NotFound"),
            @ApiResponse(responseCode = "500", ref = "ServerError")
    })
    public ResponseEntity<List<TaskListDto>> getTaskListsByProject(
            @Parameter(description = "Project ID", required = true) @PathVariable Long projectId) {
        log.debug("Fetching task lists for project with ID: {}", projectId);
        List<TaskListDto> taskLists = taskListService.findTaskListsByProject(projectId);
        return ResponseEntity.ok(taskLists);
    }

    /**
     * Updates an existing task list if the user has access.
     *
     * @param taskListId ID of the task list to update
     * @param updateDto DTO containing task list information to update
     * @return ResponseEntity containing the updated task list DTO
     */
    @PutMapping("/{taskListId}")
    @Operation(summary = "Update task list", description = "Updates an existing task list if the user has access")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Task list updated successfully",
                    content = @Content(schema = @Schema(implementation = TaskListDto.class))),
            @ApiResponse(responseCode = "400", ref = "BadRequest"),
            @ApiResponse(responseCode = "401", ref = "Unauthorized"),
            @ApiResponse(responseCode = "404", ref = "NotFound"),
            @ApiResponse(responseCode = "500", ref = "ServerError")
    })
    public ResponseEntity<TaskListDto> updateTaskList(
            @Parameter(description = "Task list ID", required = true) @PathVariable Long taskListId,
            @Valid @RequestBody TaskListUpdateDto updateDto) {
        log.info("Updating task list with ID: {}", taskListId);
        TaskListDto updatedTaskList = taskListService.updateTaskList(taskListId, updateDto);
        return ResponseEntity.ok(updatedTaskList);
    }

    /**
     * Deletes a task list if the user has access.
     *
     * @param taskListId ID of the task list to delete
     * @return ResponseEntity with no content if successful
     */
    @DeleteMapping("/{taskListId}")
    @Operation(summary = "Delete task list", description = "Deletes a task list if the user has access")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Task list deleted successfully"),
            @ApiResponse(responseCode = "401", ref = "Unauthorized"),
            @ApiResponse(responseCode = "404", ref = "NotFound"),
            @ApiResponse(responseCode = "500", ref = "ServerError")
    })
    public ResponseEntity<Void> deleteTaskList(
            @Parameter(description = "Task list ID", required = true) @PathVariable Long taskListId) {
        log.info("Deleting task list with ID: {}", taskListId);
        boolean deleted = taskListService.deleteTaskList(taskListId);
        return deleted ? ResponseEntity.noContent().build() : ResponseEntity.notFound().build();
    }
}