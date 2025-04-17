package com.example.minitrello.mapper;

import com.example.minitrello.dto.task.TaskCreateDto;
import com.example.minitrello.dto.task.TaskDto;
import com.example.minitrello.dto.task.TaskUpdateDto;
import com.example.minitrello.model.Task;
import com.example.minitrello.model.TaskList;
import com.example.minitrello.model.User;
import org.springframework.stereotype.Component;

@Component
public class TaskMapperImpl implements TaskMapper {

    @Override
    public TaskDto toDto(Task task) {
        if (task == null) {
            return null;
        }

        return TaskDto.builder()
                .id(task.getId())
                .title(task.getTitle())
                .description(task.getDescription())
                .position(task.getPosition())
                .taskListId(task.getTaskList() != null ? task.getTaskList().getId() : null)
                .taskListName(task.getTaskList() != null ? task.getTaskList().getName() : null)
                .assignedUserId(task.getAssignedUser() != null ? task.getAssignedUser().getId() : null)
                .assignedUserName(task.getAssignedUser() != null ? task.getAssignedUser().getName() : null)
                .createdAt(task.getCreatedAt())
                .updatedAt(task.getUpdatedAt())
                .build();
    }

    @Override
    public Task toEntity(TaskCreateDto createDto, TaskList taskList, User assignedUser) {
        if (createDto == null) {
            return null;
        }

        return Task.builder()
                .title(createDto.getTitle())
                .description(createDto.getDescription())
                .position(createDto.getPosition())
                .taskList(taskList)
                .assignedUser(assignedUser)
                .build();
    }

    @Override
    public void updateTaskFromDto(TaskUpdateDto updateDto, Task task) {
        if (updateDto == null || task == null) {
            return;
        }

        if (updateDto.getTitle() != null) {
            task.setTitle(updateDto.getTitle());
        }

        if (updateDto.getDescription() != null) {
            task.setDescription(updateDto.getDescription());
        }

        if (updateDto.getPosition() != null) {
            task.setPosition(updateDto.getPosition());
        }
    }
}