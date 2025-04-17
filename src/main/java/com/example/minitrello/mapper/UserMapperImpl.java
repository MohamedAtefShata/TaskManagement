package com.example.minitrello.mapper;

import com.example.minitrello.dto.auth.LoginResponse;
import com.example.minitrello.dto.auth.RegisterRequest;
import com.example.minitrello.dto.user.UserDto;
import com.example.minitrello.dto.user.UserUpdateDto;
import com.example.minitrello.model.User;
import org.springframework.stereotype.Component;

/**
 * Manual implementation of UserMapper.
 * Maps between User entities and DTOs.
 * Note: In a production app, consider using MapStruct for automatic mapping.
 */
@Component
public class UserMapperImpl implements UserMapper {

    @Override
    public UserDto toDto(User user) {
        if (user == null) {
            return null;
        }

        return UserDto.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .role(user.getRole())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .isActive(user.getIsActive())
                .projectCount(user.getProjects() != null ? user.getProjects().size() : 0)
                .assignedTaskCount(user.getAssignedTasks() != null ? user.getAssignedTasks().size() : 0)
                .build();
    }

    @Override
    public User toEntity(RegisterRequest registerRequest) {
        if (registerRequest == null) {
            return null;
        }

        return User.builder()
                .name(registerRequest.getName())
                .email(registerRequest.getEmail())
                .password(registerRequest.getPassword()) // Will be encoded in service
                .build();
    }

    @Override
    public void updateUserFromDto(UserUpdateDto updateDto, User user) {
        if (updateDto == null || user == null) {
            return;
        }

        if (updateDto.getName() != null) {
            user.setName(updateDto.getName());
        }
    }

    @Override
    public LoginResponse toLoginResponse(User user, String token) {
        if (user == null) {
            return null;
        }

        return LoginResponse.builder()
                .token(token)
                .tokenType("Bearer")
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .role(user.getRole())
                .build();
    }
}