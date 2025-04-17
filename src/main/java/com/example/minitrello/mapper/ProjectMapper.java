package com.example.minitrello.mapper;

import com.example.minitrello.dto.project.ProjectCreateDto;
import com.example.minitrello.dto.project.ProjectDto;
import com.example.minitrello.dto.project.ProjectUpdateDto;
import com.example.minitrello.model.Project;
import com.example.minitrello.model.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface ProjectMapper {

    @Mapping(target = "ownerId", source = "owner.id")
    @Mapping(target = "ownerName", source = "owner.name")
    @Mapping(target = "memberCount", expression = "java(project.getMembers().size())")
    @Mapping(target = "taskListCount", expression = "java(project.getTaskLists().size())")
    ProjectDto toDto(Project project);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "owner", source = "owner")
    @Mapping(target = "members", ignore = true)
    @Mapping(target = "taskLists", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Project toEntity(ProjectCreateDto createDto, User owner);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "owner", ignore = true)
    @Mapping(target = "members", ignore = true)
    @Mapping(target = "taskLists", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateProjectFromDto(ProjectUpdateDto updateDto, @MappingTarget Project project);
}