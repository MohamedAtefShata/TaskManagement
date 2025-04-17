package com.example.minitrello.dto.project;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Data Transfer Object for Project entity.
 * Used for transferring project data to and from the client.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProjectDto {
    private Long id;
    private String name;
    private String description;
    private Long ownerId;
    private String ownerName;

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private Integer memberCount;

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private Integer taskListCount;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}