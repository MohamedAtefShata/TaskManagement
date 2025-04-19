package com.example.minitrello.mapper;

import com.example.minitrello.dto.project.ProjectCreateDto;
import com.example.minitrello.dto.project.ProjectDto;
import com.example.minitrello.dto.project.ProjectUpdateDto;
import com.example.minitrello.dto.tasklist.TaskListDto;
import com.example.minitrello.dto.user.SimpleUserDto;
import com.example.minitrello.model.Project;
import com.example.minitrello.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Manual implementation of ProjectMapper.
 * Maps between Project entities and DTOs.
 */
@Component
public class ProjectMapperImpl implements ProjectMapper {

    @Autowired
    private TaskMapper taskMapper;

    @Autowired
    private UserMapper userMapper;

    @Override
    public ProjectDto toDto(Project project) {
        if (project == null) {
            return null;
        }

        // Create DTOs for members using UserMapper
        List<SimpleUserDto> memberDtos = project.getMembers().stream()
                .map(userMapper::toSimpleUserDto)
                .collect(Collectors.toList());

        // Create DTOs for taskLists (without including the project again to avoid circular dependency)
        List<TaskListDto> taskListDtos = project.getTaskLists().stream()
                .map(taskList -> TaskListDto.builder()
                        .id(taskList.getId())
                        .name(taskList.getName())
                        .position(taskList.getPosition())
                        .projectId(project.getId())
                        .projectName(project.getName())
                        .taskCount(taskList.getTasks() != null ? taskList.getTasks().size() : 0)
                        .tasks(taskList.getTasks() != null ?
                                taskList.getTasks().stream()
                                        .map(taskMapper::toDto)
                                        .collect(Collectors.toList()) :
                                new ArrayList<>())
                        .createdAt(taskList.getCreatedAt())
                        .updatedAt(taskList.getUpdatedAt())
                        .build())
                .collect(Collectors.toList());

        return ProjectDto.builder()
                .id(project.getId())
                .name(project.getName())
                .description(project.getDescription())
                .ownerId(project.getOwner() != null ? project.getOwner().getId() : null)
                .ownerName(project.getOwner() != null ? project.getOwner().getName() : null)
                .members(memberDtos)
                .taskLists(taskListDtos)
                .memberCount(project.getMembers() != null ? project.getMembers().size() : 0)
                .taskListCount(project.getTaskLists() != null ? project.getTaskLists().size() : 0)
                .createdAt(project.getCreatedAt())
                .updatedAt(project.getUpdatedAt())
                .build();
    }

    @Override
    public Project toEntity(ProjectCreateDto createDto, User owner) {
        if (createDto == null) {
            return null;
        }

        return Project.builder()
                .name(createDto.getName())
                .description(createDto.getDescription())
                .owner(owner)
                .build();
    }

    @Override
    public void updateProjectFromDto(ProjectUpdateDto updateDto, Project project) {
        if (updateDto == null || project == null) {
            return;
        }

        if (updateDto.getName() != null) {
            project.setName(updateDto.getName());
        }

        if (updateDto.getDescription() != null) {
            project.setDescription(updateDto.getDescription());
        }
    }
}