package com.example.minitrello.dto.tasklist;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TaskListCreateDto {
    @NotBlank(message = "Task list name is required")
    @Size(min = 3, max = 100, message = "Task list name must be between 3 and 100 characters")
    private String name;

    @NotNull(message = "Project ID is required")
    private Long projectId;

    private Integer position;
}