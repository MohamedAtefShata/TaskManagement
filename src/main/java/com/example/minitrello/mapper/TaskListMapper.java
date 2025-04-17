package com.example.minitrello.mapper;

import com.example.minitrello.dto.tasklist.TaskListCreateDto;
import com.example.minitrello.dto.tasklist.TaskListDto;
import com.example.minitrello.dto.tasklist.TaskListUpdateDto;
import com.example.minitrello.model.Project;
import com.example.minitrello.model.TaskList;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import java.util.List;

@Mapper(componentModel = "spring", uses = {TaskMapper.class})
public interface TaskListMapper {

    @Mapping(target = "projectId", source = "project.id")
    @Mapping(target = "projectName", source = "project.name")
    @Mapping(target = "taskCount", expression = "java(taskList.getTasks().size())")
    TaskListDto toDto(TaskList taskList);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "project", source = "project")
    @Mapping(target = "tasks", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    TaskList toEntity(TaskListCreateDto createDto, Project project);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "project", ignore = true)
    @Mapping(target = "tasks", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateTaskListFromDto(TaskListUpdateDto updateDto, @MappingTarget TaskList taskList);

    List<TaskListDto> toDtoList(List<TaskList> taskLists);
}