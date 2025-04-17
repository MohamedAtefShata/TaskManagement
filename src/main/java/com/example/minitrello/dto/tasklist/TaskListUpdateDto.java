package com.example.minitrello.dto.tasklist;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TaskListUpdateDto {
    @Size(min = 3, max = 100, message = "Task list name must be between 3 and 100 characters")
    private String name;

    private Integer position;
}