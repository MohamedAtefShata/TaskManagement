package com.example.minitrello.mapper;

import com.example.minitrello.dto.task.TaskCreateDto;
import com.example.minitrello.dto.task.TaskDto;
import com.example.minitrello.dto.task.TaskUpdateDto;
import com.example.minitrello.model.Task;
import com.example.minitrello.model.TaskList;
import com.example.minitrello.model.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface TaskMapper {

    @Mapping(target = "taskListId", source = "taskList.id")
    @Mapping(target = "taskListName", source = "taskList.name")
    @Mapping(target = "assignedUserId", source = "assignedUser.id")
    @Mapping(target = "assignedUserName", source = "assignedUser.name")
    TaskDto toDto(Task task);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "taskList", source = "taskList")
    @Mapping(target = "assignedUser", source = "assignedUser")
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Task toEntity(TaskCreateDto createDto, TaskList taskList, User assignedUser);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "taskList", ignore = true)
    @Mapping(target = "assignedUser", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateTaskFromDto(TaskUpdateDto updateDto, @MappingTarget Task task);
}