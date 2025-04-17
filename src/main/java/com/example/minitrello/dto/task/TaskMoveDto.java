package com.example.minitrello.dto.task;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TaskMoveDto {
    @NotNull(message = "Target task list ID is required")
    private Long targetTaskListId;

    private Integer position;
}