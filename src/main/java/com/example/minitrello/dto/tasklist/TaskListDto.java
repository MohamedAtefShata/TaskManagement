package com.example.minitrello.dto.tasklist;

import com.example.minitrello.dto.task.TaskDto;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TaskListDto {
    private Long id;
    private String name;
    private Integer position;
    private Long projectId;
    private String projectName;

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private Integer taskCount;

    private List<TaskDto> tasks;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}