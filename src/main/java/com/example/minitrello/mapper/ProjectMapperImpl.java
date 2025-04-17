package com.example.minitrello.mapper;

import com.example.minitrello.dto.project.ProjectCreateDto;
import com.example.minitrello.dto.project.ProjectDto;
import com.example.minitrello.dto.project.ProjectUpdateDto;
import com.example.minitrello.model.Project;
import com.example.minitrello.model.User;
import org.springframework.stereotype.Component;

/**
 * Manual implementation of ProjectMapper.
 * Maps between Project entities and DTOs.
 */
@Component
public class ProjectMapperImpl implements ProjectMapper {

    @Override
    public ProjectDto toDto(Project project) {
        if (project == null) {
            return null;
        }

        return ProjectDto.builder()
                .id(project.getId())
                .name(project.getName())
                .description(project.getDescription())
                .ownerId(project.getOwner() != null ? project.getOwner().getId() : null)
                .ownerName(project.getOwner() != null ? project.getOwner().getName() : null)
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