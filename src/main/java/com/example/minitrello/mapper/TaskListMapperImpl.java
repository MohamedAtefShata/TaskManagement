package com.example.minitrello.mapper;

import com.example.minitrello.dto.tasklist.TaskListCreateDto;
import com.example.minitrello.dto.tasklist.TaskListDto;
import com.example.minitrello.dto.tasklist.TaskListUpdateDto;
import com.example.minitrello.model.Project;
import com.example.minitrello.model.TaskList;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class TaskListMapperImpl implements TaskListMapper {

    @Autowired
    private TaskMapper taskMapper;

    @Override
    public TaskListDto toDto(TaskList taskList) {
        if (taskList == null) {
            return null;
        }

        return TaskListDto.builder()
                .id(taskList.getId())
                .name(taskList.getName())
                .position(taskList.getPosition())
                .projectId(taskList.getProject() != null ? taskList.getProject().getId() : null)
                .projectName(taskList.getProject() != null ? taskList.getProject().getName() : null)
                .taskCount(taskList.getTasks() != null ? taskList.getTasks().size() : 0)
                .tasks(taskList.getTasks() != null ?
                        taskList.getTasks().stream().map(taskMapper::toDto).collect(Collectors.toList()) :
                        new ArrayList<>())
                .createdAt(taskList.getCreatedAt())
                .updatedAt(taskList.getUpdatedAt())
                .build();
    }

    @Override
    public TaskList toEntity(TaskListCreateDto createDto, Project project) {
        if (createDto == null) {
            return null;
        }

        return TaskList.builder()
                .name(createDto.getName())
                .position(createDto.getPosition())
                .project(project)
                .tasks(new ArrayList<>())
                .build();
    }

    @Override
    public void updateTaskListFromDto(TaskListUpdateDto updateDto, TaskList taskList) {
        if (updateDto == null || taskList == null) {
            return;
        }

        if (updateDto.getName() != null) {
            taskList.setName(updateDto.getName());
        }

        if (updateDto.getPosition() != null) {
            taskList.setPosition(updateDto.getPosition());
        }
    }

    @Override
    public List<TaskListDto> toDtoList(List<TaskList> taskLists) {
        if (taskLists == null) {
            return null;
        }

        return taskLists.stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }
}